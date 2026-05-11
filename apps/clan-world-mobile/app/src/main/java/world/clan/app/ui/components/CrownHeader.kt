package world.clan.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import world.clan.app.R
import world.clan.app.ui.theme.ClanWorldTheme
import world.clan.app.wallet.WalletIdentity

/** Connected wallet identity, hoisted at app level via CompositionLocal. */
val LocalWalletIdentity = compositionLocalOf<WalletIdentity?> { null }

/** App-level disconnect handler. Clears the session and routes back to Connect. */
val LocalOnDisconnect = compositionLocalOf<() -> Unit> { {} }

/** App-level Devnet GOLD faucet action, exposed from the wallet menu. */
val LocalOnGoldFaucet = compositionLocalOf<() -> Unit> { {} }

/** Connected wallet's on-chain GOLD balance, whole units. Null while loading. */
val LocalGoldBalance = compositionLocalOf<Long?> { null }

/** Opens the connected wallet on Devnet Solscan. */
val LocalOnViewWallet = compositionLocalOf<() -> Unit> { {} }

/**
 * Top header: crown mark + screen name + WalletPill.
 *
 * The wallet pill (and its disconnect dropdown) is the only affordance
 * here — the previous gear icon has been removed because:
 *   - it visually read as a sun / light-mode toggle, which is misleading
 *   - it pushed Codex onto the back-stack via a non-tab navigation path,
 *     which interfered with the tab bar's `popUpTo + restoreState` logic
 *
 * Codex remains reachable via the bottom tab bar.
 */
@Composable
fun CrownHeader(
  screenName: String,
  modifier: Modifier = Modifier,
) {
  val identity = LocalWalletIdentity.current
  val onDisconnect = LocalOnDisconnect.current
  val onGoldFaucet = LocalOnGoldFaucet.current
  val goldBalance = LocalGoldBalance.current
  val onViewWallet = LocalOnViewWallet.current
  val hairline = ClanWorldTheme.colors.hairline
  val gold = ClanWorldTheme.colors.gold

  Row(
    modifier = modifier
      .padding(top = 14.dp, bottom = 14.dp)
      .drawBehind {
        // Bottom hairline
        drawLine(
          color = hairline,
          start = Offset(0f, size.height),
          end = Offset(size.width, size.height),
          strokeWidth = 1f,
        )
      },
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Left side: crown icon + screen name
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        painter = painterResource(R.drawable.ui_crown),
        contentDescription = null,
        tint = gold,
        modifier = Modifier.size(22.dp),
      )
      Text(
        text = screenName.uppercase(),
        style = ClanWorldTheme.type.crownLabel,
        color = ClanWorldTheme.colors.parchment,
      )
    }

    // Right side: wallet pill (handles its own dropdown). When no identity
    // exists (Connect screen pre-auth), the header omits this slot.
    if (identity != null) {
      WalletPill(
        identity = identity,
        goldBalance = goldBalance,
        onMintGold = onGoldFaucet,
        onViewWallet = onViewWallet,
        onDisconnect = onDisconnect,
      )
    }
  }
}
