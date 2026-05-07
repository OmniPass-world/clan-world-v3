package world.clan.app.ui

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import world.clan.app.App
import world.clan.app.ui.components.ClanWorldTabBar
import world.clan.app.ui.components.LocalOnDisconnect
import world.clan.app.ui.components.LocalWalletIdentity
import world.clan.app.ui.components.ObsidianBackground
import world.clan.app.ui.components.RootTab
import world.clan.app.ui.screens.CodexScreenRoute
import world.clan.app.ui.screens.ConnectScreenRoute
import world.clan.app.ui.screens.HallScreenRoute
import world.clan.app.ui.screens.HearthScreenRoute
import world.clan.app.ui.screens.InftDetailScreenRoute
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.viewmodel.ClanWorldViewModelFactory
import world.clan.app.viewmodel.ConnectUiState
import world.clan.app.viewmodel.ConnectViewModel
import world.clan.app.wallet.WalletIdentity

object Routes {
  const val Connect = "connect"
  const val Hearth = "hearth"
  const val Hall = "hall"
  const val Codex = "codex"
  const val InftDetail = "inft/{clanId}"
  fun inftDetail(clanId: Int) = "inft/$clanId"
}

// ─────────────────────────────────────────────────────────────────────────
// Page transitions (see plan §5.1 — half-distance slide + strong crossfade)
// ─────────────────────────────────────────────────────────────────────────

private val tabIndex = mapOf(
  Routes.Hearth to 0,
  Routes.Hall to 1,
  Routes.Codex to 2,
)

private fun NavBackStackEntry?.tabIdx(): Int? =
  tabIndex[this?.destination?.route]

private const val TAB_SLIDE_MS         = 380
private const val TAB_FADE_OUT_MS      = 220
private const val TAB_FADE_IN_MS       = 260
private const val TAB_FADE_IN_DELAY_MS = 160
private const val TAB_SLIDE_FRACTION_DIVISOR = 2

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabSlideDirection(): SlideDirection {
  val from = initialState.tabIdx()
  val to = targetState.tabIdx()
  return if (from != null && to != null && to < from) SlideDirection.End
  else SlideDirection.Start
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabEnter(): EnterTransition =
  slideIntoContainer(
    towards = tabSlideDirection(),
    animationSpec = tween(TAB_SLIDE_MS, easing = FastOutSlowInEasing),
    initialOffset = { it / TAB_SLIDE_FRACTION_DIVISOR },
  ) + fadeIn(
    tween(
      durationMillis = TAB_FADE_IN_MS,
      delayMillis = TAB_FADE_IN_DELAY_MS,
      easing = FastOutSlowInEasing,
    ),
  )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabExit(): ExitTransition =
  slideOutOfContainer(
    towards = tabSlideDirection(),
    animationSpec = tween(TAB_SLIDE_MS, easing = FastOutSlowInEasing),
    targetOffset = { it / TAB_SLIDE_FRACTION_DIVISOR },
  ) + fadeOut(tween(TAB_FADE_OUT_MS, easing = FastOutSlowInEasing))

private val connectExit: ExitTransition =
  fadeOut(tween(220, easing = FastOutSlowInEasing))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.gateRiseEnter(): EnterTransition =
  fadeIn(tween(280, easing = FastOutSlowInEasing)) +
    slideInVertically(tween(280, easing = FastOutSlowInEasing)) { it / 8 }

private fun AnimatedContentTransitionScope<NavBackStackEntry>.detailEnter(): EnterTransition =
  slideIntoContainer(
    towards = SlideDirection.Start,
    animationSpec = tween(TAB_SLIDE_MS, easing = FastOutSlowInEasing),
    initialOffset = { it / TAB_SLIDE_FRACTION_DIVISOR },
  ) + fadeIn(
    tween(
      durationMillis = TAB_FADE_IN_MS,
      delayMillis = TAB_FADE_IN_DELAY_MS,
      easing = FastOutSlowInEasing,
    ),
  )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.detailPopExit(): ExitTransition =
  slideOutOfContainer(
    towards = SlideDirection.End,
    animationSpec = tween(TAB_SLIDE_MS, easing = FastOutSlowInEasing),
    targetOffset = { it / TAB_SLIDE_FRACTION_DIVISOR },
  ) + fadeOut(tween(TAB_FADE_OUT_MS, easing = FastOutSlowInEasing))

// ─────────────────────────────────────────────────────────────────────────
// Top-level navigation host
// ─────────────────────────────────────────────────────────────────────────

@Composable
fun ClanWorldApp(app: App, hostActivity: ComponentActivity) {
  val nav = rememberNavController()
  val factory = ClanWorldViewModelFactory(app)

  // ConnectViewModel drives the start destination AND the wallet identity
  // shared with every screen. Recompute identity whenever the connected
  // pubkey changes.
  val connectVm: ConnectViewModel = viewModel(factory = factory)
  val connectState by connectVm.state.collectAsState()
  val startDestination = if (connectState.phase == ConnectUiState.Phase.Connected)
    Routes.Hearth else Routes.Connect

  val identity: WalletIdentity? by remember(connectState.solanaPubkeyBase58) {
    derivedStateOf {
      WalletIdentity.fromSession(app.sessionStore.read())
    }
  }

  // Compose-managed scope for the wallet-side fire-and-forget. Cancels
  // automatically when ClanWorldApp leaves composition, vs MainScope()
  // which leaks both the Job and captured hostActivity reference per
  // Disconnect tap.
  val coroutineScope = rememberCoroutineScope()
  val onDisconnect: () -> Unit = {
    // Snapshot the auth token before clearing the session — we need
    // it for the wallet-side disconnect call below.
    val authToken = app.sessionStore.read()?.mwaAuthToken
    // Route through ConnectViewModel.disconnect() so the VM's phase
    // flips off Connected. Otherwise ConnectScreen's auto-route on
    // Phase.Connected immediately bounces the user back to Hearth.
    connectVm.disconnect()
    nav.navigate(Routes.Connect) {
      popUpTo(0) { inclusive = true }
    }
    // Fire-and-forget wallet-side teardown so Phantom's connected-dApps
    // list clears. Failures don't matter — the local session is already
    // gone and the user is on Connect.
    if (authToken != null) {
      coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        app.mwaClient.disconnect(hostActivity, authToken)
      }
    }
  }

  // Watch the current route — drives tabbar visibility + selection.
  val currentBackStack by nav.currentBackStackEntryAsState()
  val currentRoute = currentBackStack?.destination?.route
  val showTabBar = currentRoute == Routes.Hearth ||
    currentRoute == Routes.Hall ||
    currentRoute == Routes.Codex ||
    currentRoute?.startsWith("inft/") == true
  val selectedTab = when {
    currentRoute == Routes.Hearth -> RootTab.Hearth
    currentRoute == Routes.Hall -> RootTab.Hall
    currentRoute == Routes.Codex -> RootTab.Codex
    currentRoute?.startsWith("inft/") == true -> RootTab.Hall // detail derived from Hall
    else -> RootTab.Hearth
  }

  CompositionLocalProvider(
    LocalWalletIdentity provides identity,
    LocalOnDisconnect provides onDisconnect,
  ) {
    Scaffold(
      containerColor = Color.Transparent,
      bottomBar = {
        AnimatedVisibility(
          visible = showTabBar,
          enter = slideInVertically(tween(280)) { it } + fadeIn(tween(220)),
          exit = slideOutVertically(tween(220)) { it } + fadeOut(tween(160)),
        ) {
          ClanWorldTabBar(
            selected = selectedTab,
            onSelect = { tab -> navigateTab(nav, tab) },
          )
        }
      },
    ) { padding ->
      // Persistent obsidian background underneath every route — never
      // animates with transitions. Sits behind the NavHost so the
      // entering/exiting page slides AGAINST a stationary backdrop.
      Box(modifier = Modifier.fillMaxSize()) {
        ObsidianBackground()
        NavHost(
          navController = nav,
          startDestination = startDestination,
          modifier = Modifier.fillMaxSize().padding(padding),
        ) {
          composable(
            route = Routes.Connect,
            enterTransition = { fadeIn(tween(280, easing = FastOutSlowInEasing)) },
            exitTransition = { connectExit },
          ) {
            ConnectScreenRoute(
              vm = connectVm,
              hostActivity = hostActivity,
              onConnected = {
                nav.navigate(Routes.Hearth) {
                  popUpTo(Routes.Connect) { inclusive = true }
                }
              },
            )
          }

          composable(
            route = Routes.Hearth,
            enterTransition = {
              if (initialState.destination.route == Routes.Connect) gateRiseEnter()
              else tabEnter()
            },
            exitTransition = { tabExit() },
          ) {
            HearthScreenRoute(
              app = app,
              factory = factory,
              onOpenInft = { clanId -> nav.navigate(Routes.inftDetail(clanId)) },
            )
          }

          composable(
            route = Routes.Hall,
            enterTransition = { tabEnter() },
            exitTransition = {
              if (targetState.destination.route == Routes.InftDetail)
                fadeOut(tween(TAB_FADE_OUT_MS, easing = FastOutSlowInEasing))
              else tabExit()
            },
            popEnterTransition = {
              if (initialState.destination.route == Routes.InftDetail)
                fadeIn(
                  tween(
                    durationMillis = TAB_FADE_IN_MS,
                    delayMillis = TAB_FADE_IN_DELAY_MS,
                    easing = FastOutSlowInEasing,
                  ),
                )
              else tabEnter()
            },
          ) {
            HallScreenRoute(
              app = app,
              factory = factory,
              onOpenInft = { clanId -> nav.navigate(Routes.inftDetail(clanId)) },
            )
          }

          composable(
            route = Routes.Codex,
            enterTransition = { tabEnter() },
            exitTransition = { tabExit() },
          ) {
            CodexScreenRoute(
              app = app,
              factory = factory,
            )
          }

          composable(
            route = Routes.InftDetail,
            arguments = listOf(navArgument("clanId") { type = NavType.IntType }),
            enterTransition = { detailEnter() },
            popExitTransition = { detailPopExit() },
            exitTransition = { tabExit() },
          ) { entry ->
            val clanId = entry.arguments?.getInt("clanId") ?: 1
            InftDetailScreenRoute(
              app = app,
              clanId = clanId,
              onBack = { nav.popBackStack() },
            )
          }
        }
      }
    }
  }
}

private fun navigateTab(
  nav: androidx.navigation.NavHostController,
  tab: RootTab,
) {
  val route = when (tab) {
    RootTab.Hearth -> Routes.Hearth
    RootTab.Hall -> Routes.Hall
    RootTab.Codex -> Routes.Codex
  }
  nav.navigate(route) {
    popUpTo(Routes.Hearth) { saveState = true }
    launchSingleTop = true
    restoreState = true
  }
}
