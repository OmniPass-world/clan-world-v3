package world.clan.app.cockpit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.clan.app.data.ELDERS
import world.clan.app.data.Elder
import world.clan.app.data.StubData
import world.clan.app.data.fadeOpacityForTick
import world.clan.app.ui.theme.CockpitFonts
import world.clan.app.ui.theme.CockpitTokens

/**
 * Public bulletin board flyout — slides down from beneath the header
 * when the bulletin button is toggled. Stacked list of all four clans'
 * recent + old bulletins, with corner-bracket cards mirroring the web's
 * `BulletinFlyout.tsx` aesthetic, but laid out vertically (mobile) rather
 * than the web's 2×2 grid (which would be cramped on a phone).
 *
 * Tapping outside the panel (the dim scrim) closes it.
 */
@Composable
fun BulletinFlyout(
  visible: Boolean,
  onClose: () -> Unit,
) {
  AnimatedVisibility(
    visible = visible,
    enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 4 }),
    exit  = fadeOut() + slideOutVertically(targetOffsetY = { -it / 4 }),
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // Scrim: tap-anywhere-to-close, dims the cockpit beneath.
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.55f))
          .clickable(onClick = onClose),
      )

      // Spacer for the unsafe space (CLAN/WORLD row) so the panel slides
      // in from below it, not from the very top of the screen.
      Column {
        Box(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        // Spacer for row 2 (40dp) — panel sits beneath the second header row.
        Box(modifier = Modifier.fillMaxWidth().height(40.dp))

        BulletinPanel(onClose = onClose)
      }
    }
  }
}

@Composable
private fun BulletinPanel(onClose: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(CockpitTokens.Bg.Parchment)
      .border(
        width = 1.5.dp,
        color = CockpitTokens.Bg.Ink,
      ),
  ) {
    PanelHeader(onClose = onClose)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(CockpitTokens.Space.md),
      verticalArrangement = Arrangement.spacedBy(CockpitTokens.Space.md),
    ) {
      ELDERS.forEach { elder ->
        ClanBulletinCard(elder = elder)
      }
    }
  }
}

@Composable
private fun PanelHeader(onClose: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(CockpitTokens.Bg.ParchmentDim)
      .padding(horizontal = CockpitTokens.Space.lg, vertical = CockpitTokens.Space.md),
    verticalAlignment = Alignment.Top,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = "PUBLIC BULLETIN BOARD",
        style = TextStyle(
          fontFamily = CockpitFonts.Cinzel,
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold,
          color = CockpitTokens.TextC.OnParchment,
          letterSpacing = 2.7.sp, // 0.18em
        ),
      )
      Text(
        text = "POWERED BY 0G KV STORAGE",
        modifier = Modifier.padding(top = 3.dp),
        style = TextStyle(
          fontFamily = CockpitFonts.JetBrainsMono,
          fontSize = 9.sp,
          color = CockpitTokens.TextC.OnParchmentDim,
          letterSpacing = 1.62.sp, // 0.18em
        ),
      )
    }
    Box(
      modifier = Modifier
        .padding(start = 6.dp)
        .clickable(onClick = onClose)
        .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
      Text(
        text = "✕",
        style = TextStyle(
          fontFamily = CockpitFonts.JetBrainsMono,
          fontSize = 16.sp,
          color = CockpitTokens.TextC.OnParchmentDim,
        ),
      )
    }
  }
}

@Composable
private fun ClanBulletinCard(elder: Elder) {
  val accent = elder.accent
  val posts = StubData.publicBulletins(elder.clanId)
  val visible = posts.filter { (StubData.CURRENT_TICK - it.tick) <= StubData.VISIBLE_TICKS }
  val old     = posts.filter { (StubData.CURRENT_TICK - it.tick)  > StubData.VISIBLE_TICKS }

  Box(modifier = Modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(CockpitTokens.Bg.Parchment)
        .border(1.5.dp, CockpitTokens.Bg.Ink)
        .padding(horizontal = CockpitTokens.Space.md, vertical = CockpitTokens.Space.md),
      verticalArrangement = Arrangement.spacedBy(CockpitTokens.Space.sm),
    ) {
      // Card header: clan glyph + name, accent underline
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.padding(bottom = 4.dp),
        ) {
          Text(
            text = elder.glyph,
            style = TextStyle(
              fontFamily = CockpitFonts.Cinzel,
              fontSize = 14.sp,
              color = accent,
            ),
          )
          Text(
            text = elder.name.uppercase(),
            style = TextStyle(
              fontFamily = CockpitFonts.Cinzel,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = accent,
              letterSpacing = 1.98.sp, // 0.18em
            ),
          )
        }
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(accent),
        )
      }

      visible.forEach { post -> BulletinCardRow(post = post, accent = accent, hidden = false) }

      if (old.isNotEmpty()) {
        OldSeparator()
        old.forEach { post -> BulletinCardRow(post = post, accent = accent, hidden = true) }
      }
    }
    // Corner brackets — top-left and bottom-right
    CornerBracket(modifier = Modifier.align(Alignment.TopStart), corner = Corner.TL)
    CornerBracket(modifier = Modifier.align(Alignment.BottomEnd), corner = Corner.BR)
  }
}

@Composable
private fun BulletinCardRow(post: StubData.BulletinPost, accent: Color, hidden: Boolean) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(IntrinsicSize.Min)
      .alpha(fadeOpacityForTick(post.tick))
      .background(accent.copy(alpha = 0.10f)),
  ) {
    Box(
      modifier = Modifier
        .width(2.dp)
        .fillMaxHeight()
        .background(accent),
    )
    Column(
      modifier = Modifier
        .padding(horizontal = 8.dp, vertical = 5.dp)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        VisibilityChip(hidden = hidden)
        Box(modifier = Modifier.padding(start = 6.dp))
        Text(
          text = "T${post.tick.toString().padStart(2, '0')}",
          style = TextStyle(
            fontFamily = CockpitFonts.JetBrainsMono,
            fontSize = 8.sp,
            color = CockpitTokens.TextC.Muted,
            letterSpacing = 0.8.sp,
          ),
        )
      }
      Text(
        text = post.body,
        style = TextStyle(
          fontFamily = CockpitFonts.Inter,
          fontSize = 10.sp,
          color = CockpitTokens.TextC.OnParchment,
          lineHeight = 14.sp,
        ),
      )
    }
  }
}

@Composable
private fun VisibilityChip(hidden: Boolean) {
  val (bg, fg, border, label) = if (hidden) {
    BulletinChipColors(
      bg = Color(0x2D786C54),
      fg = CockpitTokens.TextC.Muted,
      border = CockpitTokens.Border.ParchmentEdge,
      label = "OLD",
    )
  } else {
    BulletinChipColors(
      bg = Color(0x2E6AA888),
      fg = Color(0xFF3A6A4A),
      border = Color(0xFF6AA888),
      label = "VISIBLE",
    )
  }
  Box(
    modifier = Modifier
      .background(bg)
      .border(1.dp, border)
      .padding(horizontal = 5.dp, vertical = 1.dp),
  ) {
    Text(
      text = label,
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 7.sp,
        fontWeight = FontWeight.Bold,
        color = fg,
        letterSpacing = 0.56.sp,
      ),
    )
  }
}

private data class BulletinChipColors(val bg: Color, val fg: Color, val border: Color, val label: String)

@Composable
private fun OldSeparator() {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Box(
      modifier = Modifier
        .weight(1f)
        .height(1.dp)
        .background(CockpitTokens.Border.ParchmentEdge),
    )
    Text(
      text = "OLDER",
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 8.sp,
        color = CockpitTokens.TextC.Muted,
        letterSpacing = 1.44.sp, // 0.18em
      ),
    )
    Box(
      modifier = Modifier
        .weight(1f)
        .height(1.dp)
        .background(CockpitTokens.Border.ParchmentEdge),
    )
  }
}

private enum class Corner { TL, BR }

@Composable
private fun CornerBracket(modifier: Modifier = Modifier, corner: Corner) {
  val accent = CockpitTokens.TextC.Accent
  val side = 14.dp
  Box(modifier = modifier) {
    when (corner) {
      Corner.TL -> Box(
        modifier = Modifier
          .width(side)
          .height(1.5.dp)
          .background(accent),
      )
      Corner.BR -> Box(
        modifier = Modifier
          .width(side)
          .height(1.5.dp)
          .background(accent)
          .align(Alignment.BottomEnd),
      )
    }
    when (corner) {
      Corner.TL -> Box(
        modifier = Modifier
          .width(1.5.dp)
          .height(side)
          .background(accent),
      )
      Corner.BR -> Box(
        modifier = Modifier
          .width(1.5.dp)
          .height(side)
          .background(accent)
          .align(Alignment.BottomEnd),
      )
    }
  }
}
