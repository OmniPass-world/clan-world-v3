package world.clan.app

import android.app.Application
import world.clan.app.data.ClanWorldConvexClient
import world.clan.app.data.SessionStore
import world.clan.app.wallet.DeviceCapabilities
import world.clan.app.wallet.DeviceClass
import world.clan.app.wallet.MwaClient

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
  val mwaClient: MwaClient by lazy { MwaClient() }
  val sessionStore: SessionStore by lazy { SessionStore(this) }
  val deviceClass: DeviceClass by lazy { DeviceCapabilities.inspect(this) }
}
