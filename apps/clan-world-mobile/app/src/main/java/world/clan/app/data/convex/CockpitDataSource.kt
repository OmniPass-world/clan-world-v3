package world.clan.app.data.convex

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import world.clan.app.BuildConfig
import world.clan.app.cockpit.ConnectionStatus

/**
 * Owns the polling lifecycle for every Convex query the cockpit reads.
 *
 * Snapshot is polled every 5 seconds, eagerly (the header is always
 * subscribed). Per-clan tab queries are polled every 10 seconds while
 * subscribed (i.e. while the tab is composed); polling auto-stops ~5s
 * after the tab leaves composition via `WhileSubscribed(5_000)`.
 *
 * Connection status is derived from the snapshot poll's success/failure
 * counter — three consecutive failures flips to Disconnected; one
 * success resets to Connected. Initial state is Connected so the LIVE
 * pill doesn't flicker on app launch.
 */
class CockpitDataSource(
  private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) {

  private val snapshotIntervalMs = 5_000L
  private val tabIntervalMs = 10_000L

  // ---- snapshot + connection status -----------------------------------

  /** Tracks consecutive snapshot failures. Resets on success. */
  private val consecutiveFailures = MutableStateFlow(0)

  val connectionStatus: StateFlow<ConnectionStatus> =
    consecutiveFailures
      .map { fails ->
        when {
          fails == 0 -> ConnectionStatus.Connected
          fails < 3  -> ConnectionStatus.Reconnecting
          else       -> ConnectionStatus.Disconnected
        }
      }
      .stateIn(scope, SharingStarted.Eagerly, ConnectionStatus.Connected)

  val snapshot: StateFlow<QueryState<SnapshotWire>> =
    flow {
      while (true) {
        val res = ConvexClient.fetchSnapshot()
        if (res.isSuccess) {
          consecutiveFailures.value = 0
          emit(QueryState.Live(res.getOrThrow()))
        } else {
          consecutiveFailures.value = consecutiveFailures.value + 1
          emit(QueryState.Error(res.exceptionOrNull() ?: RuntimeException("snapshot failed")))
        }
        delay(snapshotIntervalMs)
      }
    }.stateIn(scope, SharingStarted.Eagerly, QueryState.Loading)

  // ---- per-clan caches ------------------------------------------------
  // Caches are accessed from the main thread (during Composable invocation
  // of useXxx) — no concurrency guard needed.

  private val vaultMovementsCache = mutableMapOf<Int, StateFlow<QueryState<List<VaultMovementWire>>>>()
  fun vaultMovements(clanId: Int): StateFlow<QueryState<List<VaultMovementWire>>> =
    vaultMovementsCache.getOrPut(clanId) {
      pollFlow { ConvexClient.fetchVaultMovements(clanId) }
    }

  private val clansmenCache = mutableMapOf<Int, StateFlow<QueryState<List<ClansmanWire>>>>()
  fun clansmen(clanId: Int): StateFlow<QueryState<List<ClansmanWire>>> =
    clansmenCache.getOrPut(clanId) {
      pollFlow { ConvexClient.fetchClansmen(clanId) }
    }

  private val memoryKvCache = mutableMapOf<Int, StateFlow<QueryState<List<MemoryKvWire>>>>()
  fun memoryKv(clanId: Int): StateFlow<QueryState<List<MemoryKvWire>>> =
    memoryKvCache.getOrPut(clanId) {
      pollFlow { ConvexClient.fetchMemoryKv(clanId) }
    }

  private val memoryEventsCache = mutableMapOf<Int, StateFlow<QueryState<List<MemoryEventWire>>>>()
  fun memoryEvents(clanId: Int): StateFlow<QueryState<List<MemoryEventWire>>> =
    memoryEventsCache.getOrPut(clanId) {
      pollFlow { ConvexClient.fetchMemoryEvents(clanId) }
    }

  private val bulletinsCache = mutableMapOf<Int, StateFlow<QueryState<List<BulletinWire>>>>()
  fun bulletins(clanId: Int): StateFlow<QueryState<List<BulletinWire>>> =
    bulletinsCache.getOrPut(clanId) {
      pollFlow { ConvexClient.fetchBulletins(clanId) }
    }

  private val combinedCommsCache = mutableMapOf<Int, StateFlow<QueryState<List<CombinedCommsWire>>>>()
  fun combinedComms(clanId: Int): StateFlow<QueryState<List<CombinedCommsWire>>> =
    combinedCommsCache.getOrPut(clanId) {
      pollFlow { ConvexClient.fetchCombinedComms(clanId) }
    }

  /**
   * Polls [fetch] every [tabIntervalMs] while subscribed. Debug builds keep
   * the demo stub fallback for empty/error results; release builds surface the
   * live empty/error state so production never shows fake backend data.
   */
  private fun <T> pollFlow(
    fetch: suspend () -> Result<List<T>>,
  ): StateFlow<QueryState<List<T>>> =
    flow<QueryState<List<T>>> {
      while (true) {
        val res = fetch()
        emit(
          res.fold(
            onSuccess = {
              if (it.isEmpty() && BuildConfig.STUB_FALLBACK_ENABLED) QueryState.Stub
              else QueryState.Live(it)
            },
            onFailure = {
              if (BuildConfig.STUB_FALLBACK_ENABLED) QueryState.Stub
              else QueryState.Error(it)
            },
          )
        )
        delay(tabIntervalMs)
      }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), QueryState.Loading)
}
