package world.clan.app.data.convex

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import world.clan.app.cockpit.ConnectionStatus

/**
 * Composition-local that exposes the live [CockpitDataSource] to every
 * tab without prop-drilling. CockpitScreen wraps its body in a
 * `CompositionLocalProvider(LocalCockpitData provides ...)`.
 */
val LocalCockpitData = staticCompositionLocalOf<CockpitDataSource> {
  error("CockpitDataSource not provided — wrap with CompositionLocalProvider in CockpitScreen")
}

@Composable
fun useSnapshot(): QueryState<SnapshotWire> {
  val ds = LocalCockpitData.current
  val state by ds.snapshot.collectAsStateWithLifecycle()
  return state
}

@Composable
fun useConnectionStatus(): ConnectionStatus {
  val ds = LocalCockpitData.current
  val state by ds.connectionStatus.collectAsStateWithLifecycle()
  return state
}

@Composable
fun useVaultMovements(clanId: Int): QueryState<List<VaultMovementWire>> {
  val ds = LocalCockpitData.current
  val state by ds.vaultMovements(clanId).collectAsStateWithLifecycle()
  return state
}

@Composable
fun useClansmen(clanId: Int): QueryState<List<ClansmanWire>> {
  val ds = LocalCockpitData.current
  val state by ds.clansmen(clanId).collectAsStateWithLifecycle()
  return state
}

@Composable
fun useMemoryKv(clanId: Int): QueryState<List<MemoryKvWire>> {
  val ds = LocalCockpitData.current
  val state by ds.memoryKv(clanId).collectAsStateWithLifecycle()
  return state
}

@Composable
fun useMemoryEvents(clanId: Int): QueryState<List<MemoryEventWire>> {
  val ds = LocalCockpitData.current
  val state by ds.memoryEvents(clanId).collectAsStateWithLifecycle()
  return state
}

@Composable
fun useBulletins(clanId: Int): QueryState<List<BulletinWire>> {
  val ds = LocalCockpitData.current
  val state by ds.bulletins(clanId).collectAsStateWithLifecycle()
  return state
}

@Composable
fun useCombinedComms(clanId: Int): QueryState<List<CombinedCommsWire>> {
  val ds = LocalCockpitData.current
  val state by ds.combinedComms(clanId).collectAsStateWithLifecycle()
  return state
}
