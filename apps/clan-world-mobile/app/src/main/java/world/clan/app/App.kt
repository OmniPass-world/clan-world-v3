package world.clan.app

import androidx.compose.runtime.mutableStateMapOf
import android.app.Application
import world.clan.app.data.ClanWorldConvexClient
import world.clan.app.data.gold.GoldSolanaClient
import world.clan.app.data.LineageStore
import world.clan.app.data.SessionStore
import world.clan.app.wallet.DeviceCapabilities
import world.clan.app.wallet.DeviceClass
import world.clan.app.wallet.MwaClient
import world.clan.app.wallet.WalletIdentity

/**
 * Application-scoped DI without Hilt — slice 1 doesn't justify the
 * compile-time cost. Each ViewModel is constructed via
 * [world.clan.app.viewmodel.ClanWorldViewModelFactory] which reads from
 * this App.
 */
class App : Application() {

  val convexClient: ClanWorldConvexClient by lazy {
    ClanWorldConvexClient(BuildConfig.CONVEX_URL)
  }
  val goldClient: GoldSolanaClient by lazy {
    GoldSolanaClient(
      rpcUrl = BuildConfig.GOLD_RPC_URL,
      mintBase58 = BuildConfig.GOLD_MINT,
      faucetProgramBase58 = BuildConfig.GOLD_FAUCET_PROGRAM_ID,
      treasuryOwnerBase58 = BuildConfig.GOLD_TREASURY_OWNER,
      decimals = BuildConfig.GOLD_DECIMALS,
    )
  }
  val mwaClient: MwaClient by lazy { MwaClient() }
  val sessionStore: SessionStore by lazy { SessionStore(this) }
  val lineageStore: LineageStore by lazy { LineageStore(this) }
  val deviceClass: DeviceClass by lazy { DeviceCapabilities.inspect(this) }

  /**
   * Session-lifetime cache of resolved wallet identities, keyed by
   * base58 pubkey. Populated by ConnectViewModel after a successful
   * MWA authorize. Compose-backed (mutableStateMapOf) so the
   * derivedStateOf in ClanWorldApp re-renders the wallet pill the
   * moment a name resolves.
   */
  val walletNameCache: androidx.compose.runtime.snapshots.SnapshotStateMap<String, WalletIdentity> =
    mutableStateMapOf()
}
