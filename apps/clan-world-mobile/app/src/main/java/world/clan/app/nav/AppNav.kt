package world.clan.app.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import world.clan.app.cockpit.CockpitScreen
import world.clan.app.owner.OwnerComingSoonScreen
import world.clan.app.owner.OwnerSignInScreen

object Routes {
  const val Cockpit          = "cockpit"
  const val OwnerSignIn      = "ownerSignIn/{clanId}"
  const val OwnerComingSoon  = "ownerComingSoon/{clanId}"

  fun ownerSignIn(clanId: Int)     = "ownerSignIn/$clanId"
  fun ownerComingSoon(clanId: Int) = "ownerComingSoon/$clanId"
}

@Composable
fun AppNav() {
  val nav = rememberNavController()

  NavHost(navController = nav, startDestination = Routes.Cockpit) {
    composable(Routes.Cockpit) {
      CockpitScreen(
        onOwnerControl = { clanId -> nav.navigate(Routes.ownerSignIn(clanId)) }
      )
    }

    composable(
      route = Routes.OwnerSignIn,
      arguments = listOf(navArgument("clanId") { type = NavType.IntType }),
    ) { entry ->
      val clanId = entry.arguments?.getInt("clanId") ?: return@composable
      OwnerSignInScreen(
        clanId = clanId,
        onSignedIn = {
          nav.navigate(Routes.ownerComingSoon(clanId)) {
            // Replace the sign-in screen so back goes to Cockpit, not back
            // through SIWS again.
            popUpTo(Routes.OwnerSignIn) { inclusive = true }
          }
        },
        onBack = { nav.popBackStack() },
      )
    }

    composable(
      route = Routes.OwnerComingSoon,
      arguments = listOf(navArgument("clanId") { type = NavType.IntType }),
    ) { entry ->
      val clanId = entry.arguments?.getInt("clanId") ?: return@composable
      OwnerComingSoonScreen(
        clanId = clanId,
        onBack = { nav.popBackStack() },
      )
    }
  }
}
