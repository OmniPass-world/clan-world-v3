package world.clan.app.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import world.clan.app.App
import world.clan.app.R
import world.clan.app.ui.components.BalanceRow
import world.clan.app.ui.components.BurnFlash
import world.clan.app.ui.components.ChatInput
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.viewmodel.FeedItem
import world.clan.app.viewmodel.SteeringConsoleUiState
import world.clan.app.viewmodel.SteeringConsoleViewModel
import world.clan.app.viewmodel.SteeringConsoleViewModelFactory
import world.clan.app.viewmodel.clanDisplayName
import world.clan.app.wallet.FakeWalletPolicy
import world.clan.app.wallet.MwaResult

private const val WHISPER_COOLDOWN_MS = 10L * 60L * 1000L  // 10 minutes
private const val WHISPER_BURN = 5L
private const val SKIP_TAX_PER_MINUTE = 1_000L
private const val FAUCET_DROP = 10_000L

/**
 * Slice 4d redesign: LLM-style chat experience for whispering to your
 * elder. Mirrors the polished web design from PR #51:
 *
 *   ── ÆLDER WHISPERS ──
 *   ↑ feed (scrolls, recent at bottom)
 *   ┌─ ChatInput ────────────────────┐
 *   │ Whisper to your Ælder…         │
 *   │ [▓░ FORGE COOLING 1:42] [⟢]   │
 *   └────────────────────────────────┘
 *   [+5 BURNED]  ← ephemeral, post-tx only
 *   GOLD · 12,450 g    [+ MINT 10K · DEVNET]
 */
@Composable
fun SteeringConsoleScreenRoute(
  app: App,
  mwaSender: ActivityResultSender,
  initialClanId: Int,
  onBack: () -> Unit,
  @Suppress("UNUSED_PARAMETER") onSent: () -> Unit,
) {
  val vm: SteeringConsoleViewModel = viewModel(
    factory = SteeringConsoleViewModelFactory(app, initialClanId),
  )
  val state by vm.state.collectAsState()
  val scope = rememberCoroutineScope()
  val view = LocalView.current

  SteeringConsole(
    state = state,
    onBack = onBack,
    onDraftChange = vm::setDraft,
    onSend = {
      scope.launch {
        vm.setSending(true)
        val token = app.sessionStore.read()?.mwaAuthToken
        if (token == null) {
          vm.setError("not signed in.")
          return@launch
        }
        val now = System.currentTimeMillis()
        val cooldownMs =
          if (state.lastSentAt < 0L) 0L
          else (WHISPER_COOLDOWN_MS - (now - state.lastSentAt)).coerceAtLeast(0L)
        val fullMinutes = ((cooldownMs + 59_999L) / 60_000L).coerceAtLeast(0L)
        val skipTax = if (cooldownMs > 0L) fullMinutes * SKIP_TAX_PER_MINUTE else 0L

        val msg = "ClanWorld Whisper — clan ${state.clanId}: ${state.draft}".toByteArray()
        val result = app.mwaClient.signMessage(mwaSender, token, msg)
        when (result) {
          is MwaResult.Ok -> {
            vm.confirmSend(
              burnAmount = WHISPER_BURN,
              skipTax = skipTax,
              sentAt = System.currentTimeMillis(),
            )
            // Native polish: confirm haptic on successful seal.
            view.performHapticFeedback(
              if (android.os.Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM
              else HapticFeedbackConstants.VIRTUAL_KEY,
            )
          }
          is MwaResult.UserDeclined -> vm.setSending(false)
          is MwaResult.WalletNotFound -> vm.setError("no wallet found on device.")
          is MwaResult.WalletNotAllowed -> vm.setError(FakeWalletPolicy.BLOCKED_MESSAGE)
          is MwaResult.Error -> vm.setError(
            result.cause.message ?: "the wallet refused the seal.",
          )
        }
      }
    },
    onFaucet = { vm.mintFaucet(FAUCET_DROP) },
    onDismissStatus = vm::clearStatus,
  )
}

@Composable
private fun SteeringConsole(
  state: SteeringConsoleUiState,
  onBack: () -> Unit,
  onDraftChange: (String) -> Unit,
  onSend: () -> Unit,
  onFaucet: () -> Unit,
  onDismissStatus: () -> Unit,
) {
  // Tick the clock locally so the cooldown chip updates without forcing
  // the parent to re-render every frame.
  val nowMs by produceState(initialValue = System.currentTimeMillis()) {
    while (true) {
      value = System.currentTimeMillis()
      delay(250L)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .imePadding(),
  ) {
    BackBar(text = clanDisplayName(state.clanId), onBack = onBack)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 22.dp),
    ) {
      OrnamentRule(label = "ÆLDER WHISPERS")

      Spacer(Modifier.height(10.dp))

      // Whisper feed — scrolls inside its own region, recent at bottom.
      WhisperFeed(
        feed = state.feed,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
      )

      Spacer(Modifier.height(10.dp))

      ChatInput(
        draft = state.draft,
        onDraftChange = onDraftChange,
        lastSentAt = state.lastSentAt,
        cooldownTotalMs = WHISPER_COOLDOWN_MS,
        nowMs = nowMs,
        gold = state.gold,
        sending = state.sending,
        burnAmount = WHISPER_BURN,
        skipTaxPerMinute = SKIP_TAX_PER_MINUTE,
        onSend = onSend,
      )

      // Ephemeral burn flash — only renders briefly after each send.
      BurnFlash(triggerKey = state.lastBurnAt, amount = state.lastBurnAmount)

      BalanceRow(
        gold = state.gold,
        bouncing = state.goldBouncing,
        faucetCooling = state.faucetCooling,
        faucetDrop = FAUCET_DROP,
        onFaucet = onFaucet,
      )

      StatusLine(
        successMsg = state.statusBody,
        errorMsg = state.errorMessage,
        onDismiss = onDismissStatus,
      )

      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun BackBar(text: String, onBack: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 22.dp, vertical = 14.dp)
      .clickable { onBack() },
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Icon(
      painter = painterResource(R.drawable.ui_arrow),
      contentDescription = "back",
      tint = ClanWorldTheme.colors.gold,
      modifier = Modifier.height(14.dp),
    )
    Text(
      text = "HALL",
      style = ClanWorldTheme.type.monoMicro,
      color = ClanWorldTheme.colors.warmDim,
    )
    Text(
      text = "·",
      style = ClanWorldTheme.type.monoMicro,
      color = ClanWorldTheme.colors.warmFaint,
    )
    Text(
      text = "the writ of ${text.split(" ").lastOrNull()?.lowercase() ?: "—"}",
      style = ClanWorldTheme.type.scriptItalic,
      color = ClanWorldTheme.colors.warmDim,
    )
  }
}

/**
 * Section header — Cinzel small-caps label flanked by hairlines. Reused
 * pattern from the slice-1 prototype; light enough to keep inline here.
 */
@Composable
private fun OrnamentRule(label: String) {
  val gold = ClanWorldTheme.colors.gold
  val hairline = ClanWorldTheme.colors.hairlineStrong

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    SideRule(hairline)
    Text(
      text = label,
      style = ClanWorldTheme.type.sectionHead,
      color = ClanWorldTheme.colors.parchment,
    )
    SideRule(hairline)
  }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.SideRule(
  color: androidx.compose.ui.graphics.Color,
) {
  Box(
    modifier = Modifier
      .weight(1f)
      .height(1.dp)
      .background(color),
  )
}

@Composable
private fun WhisperFeed(
  feed: List<FeedItem>,
  modifier: Modifier = Modifier,
) {
  if (feed.isEmpty()) {
    Box(
      modifier = modifier.padding(vertical = 12.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = "Awaiting first whisper. Press send to write.",
        style = ClanWorldTheme.type.scriptItalic,
        color = ClanWorldTheme.colors.warmFaint,
      )
    }
    return
  }
  val listState = rememberLazyListState()
  // Auto-scroll to bottom on new messages.
  LaunchedEffect(feed.size) {
    if (feed.isNotEmpty()) listState.animateScrollToItem(feed.lastIndex)
  }
  LazyColumn(
    modifier = modifier,
    state = listState,
    verticalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp),
  ) {
    items(items = feed, key = { it.key }) { item ->
      when (item) {
        is FeedItem.OwnerSent -> OwnerWhisperRow(item)
        is FeedItem.FromComms -> CommsRow(item)
      }
    }
  }
}

/** Right-aligned, ember-accent — your locally-sent whispers. */
@Composable
private fun OwnerWhisperRow(item: FeedItem.OwnerSent) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.End,
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 320.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(ClanWorldTheme.colors.iron2)
        .drawBehind {
          drawLine(
            color = androidx.compose.ui.graphics.Color(0xFFFF6B35).copy(alpha = 0.55f),
            start = Offset(size.width - 1f, 8f),
            end = Offset(size.width - 1f, size.height - 8f),
            strokeWidth = 2f,
          )
        }
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
      Text(
        text = item.body,
        style = ClanWorldTheme.type.scriptItalic.copy(color = ClanWorldTheme.colors.warm),
      )
      Text(
        text = "whispered " + relativeTime(item.sentAt),
        style = ClanWorldTheme.type.monoNano,
        color = ClanWorldTheme.colors.warmFaint,
      )
    }
  }
}

/** Left-aligned — past whispers pulled from comms:getCombinedComms. */
@Composable
private fun CommsRow(item: FeedItem.FromComms) {
  val (label, accent) = when (item.kind) {
    "human"   -> "OWNER"        to ClanWorldTheme.colors.gold
    "orch"    -> "ORCHESTRATOR" to ClanWorldTheme.colors.goldBright
    else      -> (item.speaker ?: "WHISPER").uppercase() to ClanWorldTheme.colors.rune
  }
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Start,
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 320.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(ClanWorldTheme.colors.iron)
        .drawBehind {
          drawLine(
            color = accent.copy(alpha = 0.55f),
            start = Offset(1f, 8f),
            end = Offset(1f, size.height - 8f),
            strokeWidth = 2f,
          )
        }
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
      Text(
        text = label,
        style = ClanWorldTheme.type.eyebrow.copy(fontSize = 9.sp),
        color = accent,
      )
      Text(
        text = item.body,
        style = ClanWorldTheme.type.scriptItalic.copy(color = ClanWorldTheme.colors.warm),
      )
      if (item.tick != null) {
        Text(
          text = "tick %04d".format(item.tick),
          style = ClanWorldTheme.type.monoNano,
          color = ClanWorldTheme.colors.warmFaint,
        )
      }
    }
  }
}

@Composable
private fun StatusLine(
  successMsg: String?,
  errorMsg: String?,
  onDismiss: () -> Unit,
) {
  // Auto-dismiss success after 5s.
  LaunchedEffect(successMsg) {
    if (successMsg != null) {
      delay(5_000L)
      onDismiss()
    }
  }
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(min = 18.dp)
      .clickable(enabled = errorMsg != null || successMsg != null) { onDismiss() },
    contentAlignment = Alignment.CenterStart,
  ) {
    AnimatedVisibility(
      visible = errorMsg != null,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      Text(
        text = "✕  ${errorMsg ?: ""}",
        style = ClanWorldTheme.type.monoNano,
        color = ClanWorldTheme.colors.danger,
        maxLines = Int.MAX_VALUE,
      )
    }
    AnimatedVisibility(
      visible = errorMsg == null && successMsg != null,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      Text(
        text = "✓  ${successMsg ?: ""}",
        style = ClanWorldTheme.type.monoNano,
        color = ClanWorldTheme.colors.success,
        maxLines = Int.MAX_VALUE,
      )
    }
  }
}

private fun relativeTime(thenMs: Long): String {
  val now = System.currentTimeMillis()
  val s = ((now - thenMs) / 1000L).coerceAtLeast(0L)
  if (s < 60L) return "${s}s ago"
  val m = s / 60L
  if (m < 60L) return "${m}m ago"
  val h = m / 60L
  return "${h}h ago"
}
