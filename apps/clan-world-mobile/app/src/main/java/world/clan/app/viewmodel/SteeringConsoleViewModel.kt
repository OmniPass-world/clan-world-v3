package world.clan.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.clan.app.data.ClanWorldConvexClient

/**
 * Phase of an MWA-signed write — used by SteeringConsole, StrategyEditor,
 * BazaarVm, etc. Lives here for back-compat with #66's import surface.
 */
enum class SendPhase { Idle, Signing, Queued, Failed }

/**
 * A single item in the steering console feed. Two flavours:
 *  - [OwnerSent]   — a whisper this user just sent (locally, optimistic)
 *  - [FromComms]   — a row from `comms:getCombinedComms` (past whispers
 *                    by this owner, orch broadcasts, replies from other
 *                    clans). Hydrated on screen mount.
 */
sealed interface FeedItem {
  val key: String
  data class OwnerSent(val id: Long, val body: String, val sentAt: Long) : FeedItem {
    override val key: String get() = "own-$id"
  }
  data class FromComms(
    val kind: String,
    val tick: Long?,
    val speaker: String?,
    val body: String,
    val timestamp: Long?,
    val seq: Int,
  ) : FeedItem {
    override val key: String get() = "cm-${tick ?: 0}-${speaker ?: "?"}-$seq"
  }
}

/** Back-compat alias — older code may import `Whisper`. */
typealias Whisper = FeedItem.OwnerSent

data class SteeringConsoleUiState(
  val clanId: Int,
  val draft: String = "",
  val sending: Boolean = false,
  val errorMessage: String? = null,
  val statusBody: String? = null,
  val feed: List<FeedItem> = emptyList(),
  /** Wall-clock ms when the last whisper was sent. -1 if no sends yet. */
  val lastSentAt: Long = -1L,
  /** User's GOLD balance (mocked for slice 4d — real chain integration later). */
  val gold: Long = INITIAL_GOLD,
  val sentCount: Int = 0,
  /** Wall-clock ms — drives BurnFlash mount via key-change. */
  val lastBurnAt: Long = 0L,
  val lastBurnAmount: Long = 0L,
  val faucetCooling: Boolean = false,
  val goldBouncing: Boolean = false,
) {
  companion object {
    const val INITIAL_GOLD = 14_750L
  }
}

/**
 * Slice 4d redesign of SteeringConsole — LLM-style chat experience that
 * mirrors the polished web design from PR #51 (`apps/web/src/pages/agent/`).
 *
 * State shape:
 *  - draft, sending, errorMessage  → ChatInput state
 *  - feed                          → past whispers (in-memory, this session)
 *  - lastSentAt + cooldownTotalMs  → ChatInput's cooldown chip
 *  - gold                          → ChatInput insufficient-state + BalanceRow
 *  - lastBurnAt + lastBurnAmount   → BurnFlash trigger (post-tx flash)
 *  - faucetCooling, goldBouncing   → BalanceRow faucet animation
 *
 * No real Convex mutation yet — `confirmSend()` decrements gold + appends
 * to the feed locally. When a real `convex.queueWhisper(...)` exists, swap
 * it in just before the local update.
 */
class SteeringConsoleViewModel(
  private val convex: ClanWorldConvexClient,
  initialClanId: Int,
) : ViewModel() {

  private val _state = MutableStateFlow(SteeringConsoleUiState(clanId = initialClanId))
  val state: StateFlow<SteeringConsoleUiState> = _state.asStateFlow()

  init { hydrateFeed(initialClanId) }

  /** Pull past comms for this iNFT into the feed. Best-effort. */
  private fun hydrateFeed(clanId: Int) {
    viewModelScope.launch {
      runCatching { convex.getCombinedComms(clanId, limit = 30) }
        .onSuccess { comms ->
          val items = comms
            .sortedBy { it.tick ?: 0L }                  // oldest → newest
            .mapIndexed { i, c ->
              FeedItem.FromComms(
                kind = c.kind,
                tick = c.tick,
                speaker = c.speaker,
                body = c.body,
                timestamp = c.timestamp,
                seq = i,
              )
            }
          _state.update { it.copy(feed = items + it.feed) }
        }
    }
  }

  fun setDraft(text: String) {
    _state.update { it.copy(draft = text.take(1000), errorMessage = null) }
  }

  fun setSending(sending: Boolean) {
    _state.update { it.copy(sending = sending) }
  }

  fun setError(msg: String?) {
    _state.update { it.copy(sending = false, errorMessage = msg) }
  }

  /** Optimistic update after the wallet seal succeeds. */
  fun confirmSend(burnAmount: Long, skipTax: Long, sentAt: Long) {
    val body = _state.value.draft.trim()
    if (body.isEmpty()) return
    val total = burnAmount + skipTax
    val whisper = FeedItem.OwnerSent(id = sentAt, body = body, sentAt = sentAt)
    _state.update {
      it.copy(
        draft = "",
        sending = false,
        feed = it.feed + whisper,
        lastSentAt = sentAt,
        gold = (it.gold - total).coerceAtLeast(0L),
        sentCount = it.sentCount + 1,
        lastBurnAt = sentAt,
        lastBurnAmount = burnAmount,
        statusBody = if (skipTax > 0L)
          "whisper sealed · $burnAmount burned · $skipTax g → treasury"
        else
          "whisper sealed · $burnAmount g burned",
        errorMessage = null,
      )
    }
  }

  fun clearStatus() {
    _state.update { it.copy(statusBody = null, errorMessage = null) }
  }

  fun mintFaucet(drop: Long, durationMs: Long = 1_200L) {
    if (_state.value.faucetCooling) return
    _state.update {
      it.copy(
        gold = it.gold + drop,
        faucetCooling = true,
        goldBouncing = true,
      )
    }
    viewModelScope.launch {
      kotlinx.coroutines.delay(800L)
      _state.update { it.copy(goldBouncing = false) }
      kotlinx.coroutines.delay((durationMs - 800L).coerceAtLeast(0L))
      _state.update { it.copy(faucetCooling = false) }
    }
  }
}
