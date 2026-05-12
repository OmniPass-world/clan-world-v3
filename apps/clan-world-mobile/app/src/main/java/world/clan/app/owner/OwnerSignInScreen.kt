package world.clan.app.owner

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.ComponentActivity
import kotlinx.coroutines.launch
import world.clan.app.data.elderById
import world.clan.app.owner.shared.BackChevron
import world.clan.app.ui.theme.CockpitFonts
import world.clan.app.ui.theme.CockpitTokens

/**
 * Locked-out screen — visual port of apps/web/src/pages/agent/ConnectGate.tsx.
 *
 * The "Connect Wallet" button kicks off real Solana Mobile Wallet Adapter
 * sign-in via [Mwa.signInAsOwner]. We do NOT verify the returned signature;
 * any successful return calls [onSignedIn]. The overlay shows a wax-seal
 * stamp animation while the wallet hand-off is in flight.
 *
 * **No "mock" or disclaimer copy anywhere** — this is the demo surface.
 */
@Composable
fun OwnerSignInScreen(
  clanId: Int,
  mwaSender: com.solana.mobilewalletadapter.clientlib.ActivityResultSender,
  onSignedIn: () -> Unit,
  onBack: () -> Unit,
) {
  val elder = remember(clanId) { elderById(clanId) }
  val scope = rememberCoroutineScope()
  val snackbar = remember { SnackbarHostState() }

  var loading by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.radialGradient(
          colors = listOf(
            CockpitTokens.Obsidian.Top,
            CockpitTokens.Bg.Void,
            CockpitTokens.Obsidian.Bottom,
          ),
        ),
      )
      // Keep top status-bar inset; fill behind the bottom nav bar /
      // gesture handle. Interactive elements below add their own
      // `navigationBarsPadding()` so they aren't obscured.
      .windowInsetsPadding(
        WindowInsets.systemBars.only(
          WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
        ),
      ),
  ) {
    BackChevron(onBack = onBack, modifier = Modifier.padding(start = 8.dp, top = 8.dp))

    BlurredPeekBackdrop(elder = elder)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Lockstone(
        clanId = clanId,
        clanName = elder.name,
        archetype = elder.archetype,
        glyph = elder.glyph,
        accent = elder.accent,
        oneLineEssence = elder.oneLineEssence,
        loading = loading,
        onConnect = {
          loading = true
          scope.launch {
            val result = try {
              Mwa.signInAsOwner(mwaSender, elder)
            } catch (t: Throwable) {
              SignInResult.Failed(t.message ?: "Wallet sign-in error")
            }
            loading = false
            when (result) {
              is SignInResult.Success -> onSignedIn()
              is SignInResult.Failed  -> snackbar.showSnackbar(result.reason)
            }
          }
        },
      )
    }

    SignSealOverlay(
      visible = loading,
      sigil = elder.glyph,
    )

    SnackbarHost(
      hostState = snackbar,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .navigationBarsPadding()
        .padding(16.dp),
    )
  }
}

@Composable
private fun BlurredPeekBackdrop(elder: world.clan.app.data.Elder) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .alpha(0.22f),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      text = elder.glyph,
      style = TextStyle(
        fontFamily = CockpitFonts.Cinzel,
        fontSize = 120.sp,
        color = elder.accent,
      ),
    )
    Box(modifier = Modifier.height(8.dp))
    Text(
      text = elder.name.uppercase(),
      style = TextStyle(
        fontFamily = CockpitFonts.Cinzel,
        fontSize = 32.sp,
        color = Color(0xFFE6DCCD),
        letterSpacing = 5.76.sp, // 0.18em ≈ 32 * 0.18
      ),
    )
    Box(modifier = Modifier.height(4.dp))
    Text(
      text = elder.archetype.uppercase(),
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 12.sp,
        color = CockpitTokens.Rune.Core,
        letterSpacing = 3.6.sp, // 0.3em ≈ 12 * 0.3
      ),
    )
  }
}

@Composable
private fun Lockstone(
  clanId: Int,
  clanName: String,
  archetype: String,
  glyph: String,
  accent: Color,
  oneLineEssence: String,
  loading: Boolean,
  onConnect: () -> Unit,
) {
  Column(
    modifier = Modifier
      .width(296.dp)
      .wrapContentHeight()
      .clip(RoundedCornerShape(CockpitTokens.Radius.lg))
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xEB281C14),
            Color(0xF2140E0A),
          ),
        ),
      )
      .border(1.dp, CockpitTokens.Border.IronLight, RoundedCornerShape(CockpitTokens.Radius.lg))
      .padding(horizontal = 22.dp, vertical = 28.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    RuneRingStack(glyph = glyph, accent = accent)

    Box(modifier = Modifier.height(16.dp))

    Text(
      text = "ÆLDER · ${clanId.toString().padStart(2, '0')} · SEALED",
      style = TextStyle(
        fontFamily = CockpitFonts.Inter,
        fontSize = 9.5f.sp,
        color = CockpitTokens.Rune.Core,
        letterSpacing = 4.0.sp, // ≈ 0.42em
        fontWeight = FontWeight.SemiBold,
      ),
      textAlign = TextAlign.Center,
    )
    Box(modifier = Modifier.height(6.dp))
    Text(
      text = clanName,
      style = TextStyle(
        fontFamily = CockpitFonts.Cinzel,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFE6DCCD),
        letterSpacing = 4.32.sp, // 0.18em
      ),
      textAlign = TextAlign.Center,
    )
    Box(modifier = Modifier.height(4.dp))
    Text(
      text = "“$oneLineEssence”",
      style = TextStyle(
        fontFamily = CockpitFonts.Inter,
        fontSize = 11.sp,
        color = Color(0xFFA89B85),
        letterSpacing = 1.76.sp, // 0.16em
      ),
      textAlign = TextAlign.Center,
    )
    Box(modifier = Modifier.height(20.dp))

    Text(
      text = "The Ælder's iNFT essence is sealed beneath 0G encryption. Connect a wallet bearing the matching covenant to whisper and reshape its mind.",
      style = TextStyle(
        fontFamily = CockpitFonts.Inter,
        fontSize = 11.5f.sp,
        color = Color(0xFFA89B85),
        textAlign = TextAlign.Center,
        lineHeight = 17.5f.sp,
        letterSpacing = 0.22.sp,
      ),
    )

    Box(modifier = Modifier.height(18.dp))

    ConnectWalletButton(loading = loading, onClick = onConnect)
  }
}

@Composable
private fun RuneRingStack(glyph: String, accent: Color) {
  val transition = rememberInfiniteTransition(label = "ringStack")
  val outerSpin by transition.animateFloat(
    initialValue = 0f, targetValue = 360f,
    animationSpec = infiniteRepeatable(tween(28_000, easing = LinearEasing), RepeatMode.Restart),
    label = "outer",
  )
  val innerSpin by transition.animateFloat(
    initialValue = 0f, targetValue = -360f,
    animationSpec = infiniteRepeatable(tween(18_000, easing = LinearEasing), RepeatMode.Restart),
    label = "inner",
  )

  Box(
    modifier = Modifier.size(120.dp),
    contentAlignment = Alignment.Center,
  ) {
    // Outer dashed-feel ring — Compose lacks dashed border modifier, so we
    // settle for a thin solid rune-tone ring; the spin still sells the
    // ritual feel.
    Box(
      modifier = Modifier
        .size(120.dp)
        .rotate(outerSpin)
        .border(1.dp, CockpitTokens.Rune.Deep.copy(alpha = 0.55f), CircleShape),
    )
    Box(
      modifier = Modifier
        .size(100.dp)
        .rotate(innerSpin)
        .border(1.dp, CockpitTokens.Ember.Core.copy(alpha = 0.55f), CircleShape),
    )
    Text(
      text = glyph,
      style = TextStyle(
        fontFamily = CockpitFonts.Cinzel,
        fontSize = 52.sp,
        color = accent,
      ),
    )
  }
}

@Composable
private fun ConnectWalletButton(loading: Boolean, onClick: () -> Unit) {
  val brush =
    if (loading)
      Brush.verticalGradient(listOf(CockpitTokens.Ember.Deep, Color(0xFF4A1A08)))
    else
      Brush.verticalGradient(listOf(CockpitTokens.Ember.Core, CockpitTokens.Ember.Deep))

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(CockpitTokens.Radius.md))
      .background(brush)
      .border(1.dp, CockpitTokens.Ember.Core, RoundedCornerShape(CockpitTokens.Radius.md))
      .clickable(enabled = !loading, onClick = onClick)
      .padding(horizontal = 18.dp, vertical = 13.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = if (loading) "SEALING…" else "CONNECT WALLET",
      style = TextStyle(
        fontFamily = CockpitFonts.Cinzel,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF100806),
        letterSpacing = 4.16.sp, // 0.32em
        textAlign = TextAlign.Center,
      ),
    )
  }
}
