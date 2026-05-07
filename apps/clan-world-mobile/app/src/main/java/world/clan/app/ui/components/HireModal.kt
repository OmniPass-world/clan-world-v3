package world.clan.app.ui.components

import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import world.clan.app.App
import world.clan.app.data.BazaarListing
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.ui.theme.Ink
import world.clan.app.ui.theme.Ink2
import world.clan.app.ui.theme.Ink3
import world.clan.app.ui.theme.clanColor
import world.clan.app.ui.theme.clanGlyphRes
import world.clan.app.viewmodel.clanDisplayName
import world.clan.app.wallet.MwaResult

/**
 * Hire confirmation modal. Renders as a full-screen overlay (scrim + parchment
 * card centered). On Confirm:
 *   1. Sign a "Hire" message via MWA (real signMessage call against the
 *      stored authToken).
 *   2. Show a pulsing "Sealed ✓" success state for ~1.4s.
 *   3. Call [onConfirmed] so the host can dismiss + navigate.
 *
 * If the user has no auth token (shouldn't happen on this screen — Connect
 * gates everything), Confirm short-circuits to the success state without
 * calling MWA.
 */
@Composable
fun HireModal(
  app: App,
  mwaSender: ActivityResultSender,
  listing: BazaarListing,
  onDismiss: () -> Unit,
  onConfirmed: () -> Unit,
) {
  val scope = rememberCoroutineScope()
  val state = remember { mutableStateOf(HireState.Idle) }
  val errorMessage = remember { mutableStateOf<String?>(null) }

  // Auto-dismiss after success.
  LaunchedEffect(state.value) {
    if (state.value == HireState.Sealed) {
      delay(1400L)
      onConfirmed()
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xCC050505)) // scrim
      .clickable(
        enabled = state.value == HireState.Idle || state.value == HireState.Failed,
        onClick = onDismiss,
      ),
    contentAlignment = Alignment.Center,
  ) {
    // Inner card; consume click so taps inside don't dismiss.
    Box(
      modifier = Modifier
        .padding(horizontal = 28.dp)
        .clickable(enabled = false) {},
    ) {
      ParchmentCard(
        modifier = Modifier.fillMaxWidth(),
      ) {
        HireBody(
          listing = listing,
          state = state,
          errorMessage = errorMessage,
          onConfirm = {
            scope.launch {
              state.value = HireState.Signing
              errorMessage.value = null
              val token = app.sessionStore.read()?.mwaAuthToken
              if (token == null) {
                state.value = HireState.Sealed
                return@launch
              }
              val message = "ClanWorld Hire — token 0x${"%04x".format(listing.tokenId)} clan ${listing.clanId}".toByteArray()
              val result = app.mwaClient.signMessage(mwaSender, token, message)
              when (result) {
                is MwaResult.Ok -> state.value = HireState.Sealed
                is MwaResult.WalletNotFound -> {
                  state.value = HireState.Failed
                  errorMessage.value = "no wallet found on device."
                }
                is MwaResult.UserDeclined -> {
                  state.value = HireState.Idle
                }
                is MwaResult.Error -> {
                  state.value = HireState.Failed
                  errorMessage.value = result.cause.message ?: "the wallet refused the seal."
                }
              }
            }
          },
          onDismiss = onDismiss,
        )
      }
    }
  }
}

private enum class HireState { Idle, Signing, Sealed, Failed }

@Composable
private fun HireBody(
  listing: BazaarListing,
  state: MutableState<HireState>,
  errorMessage: MutableState<String?>,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  val clanCol = clanColor(listing.clanId)

  Row(
    verticalAlignment = Alignment.Top,
    horizontalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Column(Modifier.weight(1f)) {
      Text(
        text = "BIND THE CONTRACT",
        style = ClanWorldTheme.type.monoMicro,
        color = Ink3,
      )
      Text(
        text = clanDisplayName(listing.clanId),
        style = ClanWorldTheme.type.letterName,
        color = Ink,
        modifier = Modifier.padding(top = 2.dp),
      )
      Text(
        text = "tkn 0x${"%04x".format(listing.tokenId)}",
        style = ClanWorldTheme.type.monoMicro,
        color = Ink3,
        modifier = Modifier.padding(top = 2.dp),
      )
    }
    WaxSeal(
      glyph = painterResource(clanGlyphRes(listing.clanId)),
      clanColor = clanCol,
    )
  }

  Spacer(Modifier.height(12.dp))

  Text(
    text = listing.pitch,
    style = ClanWorldTheme.type.scriptItalic,
    color = Ink2,
  )

  Spacer(Modifier.height(14.dp))

  // Stat strip
  Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    HireStat(label = "Price", value = "${listing.pricePerSeason}g", color = ClanWorldTheme.colors.gold)
    HireStat(label = "Term", value = "i season", color = Ink)
    HireStat(label = "Owner", value = listing.ownerShort, color = Ink3)
  }

  Spacer(Modifier.height(18.dp))

  when (state.value) {
    HireState.Idle, HireState.Failed -> {
      EmberCta(
        text = if (state.value == HireState.Failed) "Try Again" else "Sign with Wallet",
        onClick = onConfirm,
        modifier = Modifier.fillMaxWidth(),
      )
      if (state.value == HireState.Failed) {
        Spacer(Modifier.height(8.dp))
        Text(
          text = errorMessage.value ?: "—",
          style = ClanWorldTheme.type.scriptItalic,
          color = ClanWorldTheme.colors.danger,
        )
      }
      Spacer(Modifier.height(8.dp))
      Text(
        text = "DISMISS",
        style = ClanWorldTheme.type.monoNano,
        color = Ink3,
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onDismiss() }
          .padding(vertical = 10.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
      )
    }
    HireState.Signing -> {
      Box(
        Modifier
          .fillMaxWidth()
          .height(54.dp)
          .clip(RoundedCornerShape(6.dp))
          .drawBehind {
            drawRoundRect(
              color = Ink.copy(alpha = 0.10f),
              cornerRadius = CornerRadius(6.dp.toPx()),
              style = Stroke(width = 1.dp.toPx()),
            )
          },
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "AWAITING THE WALLET'S SEAL…",
          style = ClanWorldTheme.type.ctaLabel,
          color = Ink3,
        )
      }
    }
    HireState.Sealed -> {
      Box(
        Modifier
          .fillMaxWidth()
          .height(54.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(ClanWorldTheme.colors.success.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "SEALED ✓",
          style = ClanWorldTheme.type.ctaLabel,
          color = ClanWorldTheme.colors.success,
        )
      }
    }
  }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.HireStat(
  label: String,
  value: String,
  color: Color,
) {
  Column(Modifier.weight(1f)) {
    Text(
      text = label.uppercase(),
      style = ClanWorldTheme.type.monoNano,
      color = Ink3,
    )
    Text(
      text = value,
      style = ClanWorldTheme.type.monoData,
      color = color,
      modifier = Modifier.padding(top = 2.dp),
    )
  }
}
