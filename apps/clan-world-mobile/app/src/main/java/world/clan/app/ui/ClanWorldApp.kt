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
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import world.clan.app.App
import world.clan.app.cockpit.CockpitScreen
import world.clan.app.owner.OwnerComingSoonScreen
import world.clan.app.owner.OwnerSignInScreen
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
  const val Bazaar = "bazaar"
  const val Codex = "codex"
  const val InftDetail = "inft/{clanId}"
  const val BazaarInftDetail = "bazaarInft/{clanId}"
  const val Whispers = "whispers/{clanId}"
  const val SteeringConsole = "steer/{clanId}"
  const val StrategyEditor = "strategy/{clanId}"
  const val Treasury = "treasury/{clanId}"
  const val Forge = "forge"
  const val Forged = "forged/{clanId}?name={name}&label={label}"
  const val Bridge = "bridge/{clanId}"
  const val Cockpit = "cockpit/{clanId}"
  const val OwnerSignIn = "ownerSignIn/{clanId}"
  const val OwnerComingSoon = "ownerComingSoon/{clanId}"
  fun inftDetail(clanId: Int) = "inft/$clanId"
  fun bazaarInftDetail(clanId: Int) = "bazaarInft/$clanId"
  fun whispers(clanId: Int) = "whispers/$clanId"
  fun steer(clanId: Int) = "steer/$clanId"
  fun strategy(clanId: Int) = "strategy/$clanId"
  fun treasury(clanId: Int) = "treasury/$clanId"
  fun forged(clanId: Int, name: String = "", label: String = "FORGED"): String {
    val n = java.net.URLEncoder.encode(name, "UTF-8")
    val l = java.net.URLEncoder.encode(label, "UTF-8")
    return "forged/$clanId?name=$n&label=$l"
  }
  fun bridge(clanId: Int) = "bridge/$clanId"
  fun cockpit(clanId: Int) = "cockpit/$clanId"
  fun ownerSignIn(clanId: Int) = "ownerSignIn/$clanId"
  fun ownerComingSoon(clanId: Int) = "ownerComingSoon/$clanId"
}

// ─────────────────────────────────────────────────────────────────────────
// Page transitions (see plan §5.1 — half-distance slide + strong crossfade)
// ─────────────────────────────────────────────────────────────────────────

private val tabIndex = mapOf(
  Routes.Hearth to 0,
  Routes.Hall to 1,
  Routes.Bazaar to 2,
  Routes.Codex to 3,
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
fun ClanWorldApp(
  app: App,
  hostActivity: ComponentActivity,
  mwaSender: ActivityResultSender,
) {
  val nav = rememberNavController()
  val factory = ClanWorldViewModelFactory(app)

  // ConnectViewModel drives the start destination AND the wallet identity
  // shared with every screen. Recompute identity whenever the connected
  // pubkey changes.
  val connectVm: ConnectViewModel = viewModel(factory = factory)
  val connectState by connectVm.state.collectAsState()
  // v0.2.1 demo: skip the Connect/Seed Vault entry screen — MWA flow is broken
  // and the demo flows don't require a wallet identity for read-only screens.
  // Users that need wallet ops can reach Connect via Disconnect→onDisconnect.
  val startDestination = Routes.Hearth

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
        app.mwaClient.disconnect(mwaSender, authToken)
      }
    }
  }

  // Watch the current route — drives tabbar visibility + selection.
  val currentBackStack by nav.currentBackStackEntryAsState()
  val currentRoute = currentBackStack?.destination?.route
  val showTabBar = currentRoute == Routes.Hearth ||
    currentRoute == Routes.Hall ||
    currentRoute == Routes.Bazaar ||
    currentRoute == Routes.Codex ||
    currentRoute?.startsWith("inft/") == true ||
    currentRoute?.startsWith("bazaarInft/") == true ||
    currentRoute?.startsWith("whispers/") == true ||
    currentRoute?.startsWith("strategy/") == true ||
    currentRoute?.startsWith("treasury/") == true ||
    currentRoute == Routes.Forge
  // Cockpit + Owner sign-in / coming-soon are full-screen flows: tabbar hidden.
  val selectedTab = when {
    currentRoute == Routes.Hearth -> RootTab.Hearth
    currentRoute == Routes.Hall -> RootTab.Hall
    currentRoute == Routes.Bazaar -> RootTab.Bazaar
    currentRoute == Routes.Codex -> RootTab.Codex
    currentRoute?.startsWith("inft/") == true -> RootTab.Hall // detail derived from Hall
    currentRoute?.startsWith("bazaarInft/") == true -> RootTab.Bazaar // detail derived from Bazaar
    currentRoute?.startsWith("whispers/") == true -> RootTab.Hall // inbox is a Hall drill-in
    currentRoute?.startsWith("strategy/") == true -> RootTab.Hall // editor is a Hall drill-in
    currentRoute?.startsWith("treasury/") == true -> RootTab.Hall // treasury is a Hall drill-in
    currentRoute == Routes.Forge -> RootTab.Hall // forge wizard surfaces from Hall
    currentRoute?.startsWith("steer/") == true ||
      currentRoute?.startsWith("bridge/") == true ||
      currentRoute?.startsWith("cockpit/") == true ||
      currentRoute?.startsWith("ownerSignIn/") == true ||
      currentRoute?.startsWith("ownerComingSoon/") == true -> RootTab.Hall
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
              mwaSender = mwaSender,
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
              onForge = { nav.navigate(Routes.Forge) },
            )
          }

          composable(
            route = Routes.Bazaar,
            enterTransition = { tabEnter() },
            exitTransition = {
              if (targetState.destination.route == Routes.BazaarInftDetail)
                fadeOut(tween(TAB_FADE_OUT_MS, easing = FastOutSlowInEasing))
              else tabExit()
            },
            popEnterTransition = {
              if (initialState.destination.route == Routes.BazaarInftDetail)
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
            world.clan.app.ui.screens.BazaarScreenRoute(
              app = app,
              factory = factory,
              onOpenListing = { clanId -> nav.navigate(Routes.bazaarInftDetail(clanId)) },
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
              onEnterCockpit = { nav.navigate(Routes.bridge(clanId)) },
              onOpenInbox = { nav.navigate(Routes.whispers(clanId)) },
              onEditStrategy = { nav.navigate(Routes.strategy(clanId)) },
              onOpenTreasury = { nav.navigate(Routes.treasury(clanId)) },
            )
          }

          // ── Bazaar iNFT detail (preview a hireable sigil; "Hire" instead
          //    of "Enter Cockpit"). Confirms via HireModal → SIWS sign.
          composable(
            route = Routes.BazaarInftDetail,
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
              onOpenInbox = { nav.navigate(Routes.whispers(clanId)) },
              onOpenTreasury = { nav.navigate(Routes.treasury(clanId)) },
              isBazaar = true,
              mwaSender = mwaSender,
              onHireConfirmed = {
                val listing = world.clan.app.data.bazaarListingByClan(clanId)
                val name = listing?.let { "tkn 0x${"%04x".format(it.tokenId)}" }.orEmpty()
                app.lineageStore.append(
                  world.clan.app.data.LineageEntry(
                    kind = "hired",
                    clanId = clanId,
                    title = "Hired ${world.clan.app.viewmodel.clanDisplayName(clanId)}",
                    subtitle = listing?.let { "${it.pricePerSeason}g · 1 season · $name" } ?: name,
                  ),
                )
                // Persist that this clan is now in the user's hall — so
                // it shows up next time HallScreen refreshes (also affects
                // Hearth leaderboard `isMine` and Codex linked-clans line).
                app.sessionStore.addHiredClanId(clanId)
                nav.navigate(Routes.forged(clanId, name, "HIRED")) {
                  popUpTo(Routes.Bazaar) { saveState = true }
                }
              },
            )
          }

          // ── Bridge loader (between InftDetail "Enter Cockpit" and Cockpit) ─
          composable(
            route = Routes.Bridge,
            arguments = listOf(navArgument("clanId") { type = NavType.IntType }),
            enterTransition = { fadeIn(tween(280, easing = FastOutSlowInEasing)) },
            exitTransition = { fadeOut(tween(280, easing = FastOutSlowInEasing)) },
            popExitTransition = { fadeOut(tween(220, easing = FastOutSlowInEasing)) },
          ) { entry ->
            val clanId = entry.arguments?.getInt("clanId") ?: 1
            world.clan.app.ui.screens.BridgeScreen(
              clanId = clanId,
              onReady = {
                nav.navigate(Routes.cockpit(clanId)) {
                  popUpTo(Routes.Bridge) { inclusive = true }
                }
              },
            )
          }

          // ── Whispers inbox (deep route from InftDetail Whispers tab) ────
          composable(
            route = Routes.Whispers,
            arguments = listOf(navArgument("clanId") { type = NavType.IntType }),
            enterTransition = { detailEnter() },
            popExitTransition = { detailPopExit() },
            exitTransition = { tabExit() },
          ) { entry ->
            val clanId = entry.arguments?.getInt("clanId") ?: 1
            world.clan.app.ui.screens.WhispersScreenRoute(
              app = app,
              clanId = clanId,
              onBack = { nav.popBackStack() },
              onCompose = { nav.navigate(Routes.steer(clanId)) },
            )
          }

          // ── SteeringConsole (whisper composer; deep route from Whispers) ─
          composable(
            route = Routes.SteeringConsole,
            arguments = listOf(navArgument("clanId") { type = NavType.IntType }),
            enterTransition = { detailEnter() },
            popExitTransition = { detailPopExit() },
            exitTransition = { tabExit() },
          ) { entry ->
            val clanId = entry.arguments?.getInt("clanId") ?: 1
            world.clan.app.ui.screens.SteeringConsoleScreenRoute(
              app = app,
              mwaSender = mwaSender,
              initialClanId = clanId,
              onBack = { nav.popBackStack() },
              onSent = {
                app.lineageStore.append(
                  world.clan.app.data.LineageEntry(
                    kind = "whispered",
                    clanId = clanId,
                    title = "Whispered to ${world.clan.app.viewmodel.clanDisplayName(clanId)}",
                    subtitle = "queued for next tick",
                  ),
                )
                nav.popBackStack()
              },
            )
          }

          // ── Forge (4-step mint wizard; surfaces from Hall) ──────────────
          composable(
            route = Routes.Forge,
            enterTransition = { detailEnter() },
            popExitTransition = { detailPopExit() },
            exitTransition = { tabExit() },
          ) {
            world.clan.app.ui.screens.ForgeScreenRoute(
              app = app,
              factory = factory,
              mwaSender = mwaSender,
              onBack = { nav.popBackStack() },
              onForged = { clanId, name ->
                app.lineageStore.append(
                  world.clan.app.data.LineageEntry(
                    kind = "forged",
                    clanId = clanId,
                    title = "Forged ${name.ifBlank { "the unnamed seal" }}",
                    subtitle = world.clan.app.viewmodel.clanDisplayName(clanId),
                  ),
                )
                // Persist that this clan is now in the user's hall — same
                // mechanism as Bazaar Hire. Hall + Hearth + Codex unions
                // pick this up on next refresh.
                app.sessionStore.addForgedClanId(clanId)
                nav.navigate(Routes.forged(clanId, name, "FORGED")) {
                  popUpTo(Routes.Forge) { inclusive = true }
                }
              },
            )
          }

          // ── Forged (celebration landing for Forge + Hire) ───────────────
          composable(
            route = Routes.Forged,
            arguments = listOf(
              androidx.navigation.navArgument("clanId") { type = NavType.IntType },
              androidx.navigation.navArgument("name") {
                type = NavType.StringType
                nullable = true
                defaultValue = ""
              },
              androidx.navigation.navArgument("label") {
                type = NavType.StringType
                nullable = true
                defaultValue = "FORGED"
              },
            ),
            enterTransition = { fadeIn(tween(360, easing = FastOutSlowInEasing)) },
            popExitTransition = { fadeOut(tween(220, easing = FastOutSlowInEasing)) },
          ) { entry ->
            val clanId = entry.arguments?.getInt("clanId") ?: 1
            val name = entry.arguments?.getString("name").orEmpty().let {
              runCatching { java.net.URLDecoder.decode(it, "UTF-8") }.getOrDefault(it)
            }
            val label = (entry.arguments?.getString("label") ?: "FORGED").let {
              runCatching { java.net.URLDecoder.decode(it, "UTF-8") }.getOrDefault(it)
            }
            world.clan.app.ui.screens.ForgedScreen(
              clanId = clanId,
              label = label,
              sigilName = name,
              onEnterHall = {
                nav.navigate(Routes.Hall) {
                  popUpTo(Routes.Hearth) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              },
            )
          }

          // ── Treasury (per-iNFT GOLD + resources + movements; from Vault tab) ─
          composable(
            route = Routes.Treasury,
            arguments = listOf(navArgument("clanId") { type = NavType.IntType }),
            enterTransition = { detailEnter() },
            popExitTransition = { detailPopExit() },
            exitTransition = { tabExit() },
          ) { entry ->
            val clanId = entry.arguments?.getInt("clanId") ?: 1
            world.clan.app.ui.screens.TreasuryScreenRoute(
              app = app,
              clanId = clanId,
              onBack = { nav.popBackStack() },
            )
          }

          // ── StrategyEditor (per-iNFT doctrine form; deep route from InftDetail) ─
          composable(
            route = Routes.StrategyEditor,
            arguments = listOf(navArgument("clanId") { type = NavType.IntType }),
            enterTransition = { detailEnter() },
            popExitTransition = { detailPopExit() },
            exitTransition = { tabExit() },
          ) { entry ->
            val clanId = entry.arguments?.getInt("clanId") ?: 1
            world.clan.app.ui.screens.StrategyEditorScreenRoute(
              app = app,
              mwaSender = mwaSender,
              clanId = clanId,
              onBack = { nav.popBackStack() },
              onSaved = {
                app.lineageStore.append(
                  world.clan.app.data.LineageEntry(
                    kind = "sealed",
                    clanId = clanId,
                    title = "Sealed doctrine for ${world.clan.app.viewmodel.clanDisplayName(clanId)}",
                    subtitle = "the elder will hold this counsel",
                  ),
                )
                nav.popBackStack()
              },
            )
          }

          // ── Cockpit (deep route from Bridge after the loader resolves) ──
          composable(
            route = Routes.Cockpit,
            arguments = listOf(navArgument("clanId") { type = NavType.IntType }),
            enterTransition = { detailEnter() },
            popExitTransition = { detailPopExit() },
            exitTransition = { tabExit() },
          ) { entry ->
            val clanId = entry.arguments?.getInt("clanId") ?: 1
            CockpitScreen(
              initialClanId = clanId,
              onOwnerControl = { c ->
                nav.navigate(Routes.ownerSignIn(c))
              },
            )
          }

          // ── Owner sign-in (drill-in from CockpitScreen) ─────────────────
          composable(
            route = Routes.OwnerSignIn,
            arguments = listOf(navArgument("clanId") { type = NavType.IntType }),
            enterTransition = { detailEnter() },
            popExitTransition = { detailPopExit() },
            exitTransition = { tabExit() },
          ) { entry ->
            val clanId = entry.arguments?.getInt("clanId") ?: 1
            OwnerSignInScreen(
              clanId = clanId,
              onSignedIn = {
                nav.navigate(Routes.ownerComingSoon(clanId)) {
                  // Replace the sign-in screen so back goes to Cockpit,
                  // not back through SIWS again.
                  popUpTo(Routes.OwnerSignIn) { inclusive = true }
                }
              },
              onBack = { nav.popBackStack() },
            )
          }

          // ── Owner coming-soon (placeholder after successful SIWS) ───────
          composable(
            route = Routes.OwnerComingSoon,
            arguments = listOf(navArgument("clanId") { type = NavType.IntType }),
            enterTransition = { detailEnter() },
            popExitTransition = { detailPopExit() },
            exitTransition = { tabExit() },
          ) { entry ->
            val clanId = entry.arguments?.getInt("clanId") ?: 1
            OwnerComingSoonScreen(
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
    RootTab.Bazaar -> Routes.Bazaar
    RootTab.Codex -> Routes.Codex
  }
  nav.navigate(route) {
    popUpTo(Routes.Hearth) { saveState = true }
    launchSingleTop = true
    restoreState = true
  }
}
