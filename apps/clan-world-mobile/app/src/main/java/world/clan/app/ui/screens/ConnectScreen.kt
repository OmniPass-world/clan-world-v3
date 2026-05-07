package world.clan.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import world.clan.app.ui.components.ConnectSigilSpec
import world.clan.app.ui.components.EmberCta
import world.clan.app.ui.components.ObsidianBackground
import world.clan.app.ui.components.OrnamentRule
import world.clan.app.ui.components.Sigil
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.viewmodel.ConnectUiState
import world.clan.app.viewmodel.ConnectViewModel

/**
 * Route wrapper. Receives the [ActivityResultSender] (constructed in
 * MainActivity.onCreate before super) and forwards it into the ViewModel.
 */
@Composable
fun ConnectScreenRoute(
  vm: ConnectViewModel,
  mwaSender: ActivityResultSender,
  onConnected: () -> Unit,
) {
  val state by vm.state.collectAsState()

  // Watch for transition Idle/Connecting → Connected, then route forward.
  LaunchedEffect(state.phase) {
    if (state.phase == ConnectUiState.Phase.Connected) {
      // Tiny pause so the user sees the success state, then advance.
      kotlinx.coroutines.delay(300)
      onConnected()
    }
  }

  // v0.2.0 demo regression fix — the cold-launch auto-reauthorize was
  // racing with manual taps + producing a "tap → toast → bounce back to
  // Connect" loop on devices with a stale stored session. Demo expects
  // that "Open Seed Vault" is the SOLE trigger for any wallet activity.
  // Stale sessions are recovered manually: tap → reauthorize → if it
  // fails the VM clears the session and the next tap is a clean connect.
  // Re-enable for v0.2.x once the silent-flash UX is reproducible.

  ConnectScreen(
    state = state,
    onConnect = { vm.connect(mwaSender) },
  )
}

@Composable
private fun ConnectScreen(
  state: ConnectUiState,
  onConnect: () -> Unit,
) {
  val gold = ClanWorldTheme.colors.gold
  val ember = ClanWorldTheme.colors.ember
  val parchment = ClanWorldTheme.colors.parchment
  val warm = ClanWorldTheme.colors.warm
  val warmDim = ClanWorldTheme.colors.warmDim
  val warmFaint = ClanWorldTheme.colors.warmFaint

  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    // Layer 0: obsidian + grain + radial gradients
    ObsidianBackground(emberAlpha = 0.10f, runeAlpha = 0.04f, goldAlpha = 0.04f)

    // Layer 1: focused vignette around the sigil
    Box(
      Modifier
        .fillMaxSize()
        .drawBehind {
          drawRect(
            brush = Brush.radialGradient(
              0.30f to Color.Transparent,
              1.0f to Color.Black.copy(alpha = 0.5f),
              center = Offset(size.width / 2f, size.height * 0.42f),
              radius = size.width * 0.85f,
            ),
          )
        },
    )

    // Layer 2: content
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(WindowInsets.statusBars.asPaddingValues())
        .padding(horizontal = 36.dp, vertical = 64.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      // ── Top: ornament + wordmark + ornament ──────────────────────────
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(top = 30.dp),
      ) {
        OrnamentRule(lineWidth = 24.dp, lozengeCount = 1, color = gold)
        Text(
          text = buildAnnotatedString {
            withStyle(
              SpanStyle(
                color = parchment,
                fontFamily = world.clan.app.ui.theme.Cinzel,
                fontWeight = FontWeight.Medium,
              ),
            ) { append("CLAN  WORLD") }
            append("  ")
            withStyle(
              SpanStyle(
                color = ember,
                fontFamily = world.clan.app.ui.theme.CormorantItalic,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                fontWeight = FontWeight.Medium,
              ),
            ) { append("·") }
          },
          style = ClanWorldTheme.type.displayHero.copy(letterSpacing = 0.42.em),
        )
        OrnamentRule(lineWidth = 42.dp, lozengeCount = 3, color = gold)
      }

      // ── Sigil ─────────────────────────────────────────────────────────
      Sigil(
        spec = ConnectSigilSpec,
        modifier = Modifier.size(230.dp),
        animated = true,
      )

      // ── Invocation ────────────────────────────────────────────────────
      Text(
        text = buildAnnotatedString {
          append("The eight realms wait.\n")
          append("Open your ")
          withStyle(
            SpanStyle(color = gold, fontWeight = FontWeight.Medium),
          ) { append("Seed Vault") }
          append(", traveller,\nand the Hearth shall recognize you.")
        },
        style = ClanWorldTheme.type.scriptInvocation,
        color = warm,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(0.85f),
      )

      // ── Bottom: CTA + secondary + status ──────────────────────────────
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        EmberCta(
          text = when {
            state.phase == ConnectUiState.Phase.Connecting && state.pendingVerification ->
              "Resuming sigil…"
            state.phase == ConnectUiState.Phase.Connecting ->
              "Calling the seed vault…"
            state.phase == ConnectUiState.Phase.Connected ->
              "Recognized · entering the hearth"
            else -> "Open Seed Vault"
          },
          onClick = onConnect,
          enabled = state.phase != ConnectUiState.Phase.Connecting &&
            state.phase != ConnectUiState.Phase.Connected,
          modifier = Modifier.fillMaxWidth(),
        )
        if (state.errorMessage != null) {
          Text(
            text = state.errorMessage,
            style = ClanWorldTheme.type.scriptItalicSmall,
            color = ClanWorldTheme.colors.danger,
            textAlign = TextAlign.Center,
          )
        } else {
          Text(
            text = "use a paired Solana wallet instead",
            style = ClanWorldTheme.type.scriptItalicSmall,
            color = warmDim,
            modifier = Modifier
              .drawBehind {
                drawLine(
                  color = warmFaint,
                  start = Offset(0f, size.height + 2f),
                  end = Offset(size.width, size.height + 2f),
                  strokeWidth = 1f,
                )
              },
          )
        }
        Spacer(Modifier.height(2.dp))
        Text(
          text = "v 0.1.0   ·   Slice I   ·   Seeker · Pixel",
          style = ClanWorldTheme.type.monoNano,
          color = warmFaint,
        )
      }
    }
  }
}
