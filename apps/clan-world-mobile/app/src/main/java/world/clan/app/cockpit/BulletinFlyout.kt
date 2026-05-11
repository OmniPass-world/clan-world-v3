package world.clan.app.cockpit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.clan.app.BuildConfig
import world.clan.app.data.ELDERS
import world.clan.app.data.Elder
import world.clan.app.data.StubData
import world.clan.app.data.convex.QueryState
import world.clan.app.data.convex.toPublicPost
import world.clan.app.data.convex.useBulletins
import world.clan.app.data.fadeOpacityForTick
import world.clan.app.ui.theme.CockpitFonts
import world.clan.app.ui.theme.CockpitTokens

// Shared constants
private val PanelCorner = 8.dp
private val PanelElevation = 16.dp
private val CardBg = CockpitTokens.Bg.ParchmentDim
private val CardCorner = 4.dp

/**
 * Public bulletin board — floats below the header, attached visually to
 * the centred BULLETINS button. The shadow extends on all four sides,
 * but the top shadow falls behind the header (which is drawn AFTER this
 * composable in CockpitScreen's z-order), so visually only the left,
 * right, and bottom shadows show — like a clipped page hung from the
 * bottom of the header.
 *
 * Sizing — driven by orientation:
 *  - portrait : 85% screen width, ~90% screen height, centred (margins
 *    on both sides + bottom). Cards stack 1 column.
 *  - landscape: 60% screen width, full height below the header. Cards
 *    laid out as a 2×2 grid.
 *
 * Each card is identical-sized (weight=1f within its row/column) and
 * scrolls internally; the panel itself never grows.
 */
@Composable
fun BulletinFlyout(
  visible: Boolean,
  onClose: () -> Unit,
) {
  val config = LocalConfiguration.current
  val screenW = config.screenWidthDp.dp
  val screenH = config.screenHeightDp.dp
  val isLandscape = screenW > screenH

  val widthFraction = if (isLandscape) 0.60f else 0.85f
  val heightFraction = if (isLandscape) 1.00f else 0.90f
  val panelW = screenW * widthFraction
  val statusBarH = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
  val headerH = statusBarH + HeaderRow2Height
  val panelH = (screenH * heightFraction) - headerH

  Box(modifier = Modifier.fillMaxSize()) {
    // Dim scrim (separate fade from the panel's expand animation)
    AnimatedVisibility(
      visible = visible,
      enter = fadeIn(animationSpec = tween(220)),
      exit  = fadeOut(animationSpec = tween(180)),
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.55f))
          .clickable(onClick = onClose),
      )
    }

    // Floating panel — expands from a TopCentre origin (the position of
    // the BULLETINS button), so it reads as "unscrolling out of" the
    // header rather than sliding in from off-screen.
    AnimatedVisibility(
      visible = visible,
      enter = fadeIn(animationSpec = tween(220)) +
        expandIn(
          expandFrom = Alignment.TopCenter,
          animationSpec = tween(280),
        ),
      exit = fadeOut(animationSpec = tween(160)) +
        shrinkOut(
          shrinkTowards = Alignment.TopCenter,
          animationSpec = tween(200),
        ),
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = headerH),
    ) {
      Box(
        modifier = Modifier
          .width(panelW)
          .height(panelH)
          .shadow(
            elevation = PanelElevation,
            shape = RoundedCornerShape(PanelCorner),
            clip = false,
          )
          .clip(RoundedCornerShape(PanelCorner))
          .background(CockpitTokens.Bg.Parchment)
          .border(1.dp, CockpitTokens.Bg.Ink, RoundedCornerShape(PanelCorner)),
      ) {
        BulletinPanel(isLandscape = isLandscape, onClose = onClose)
      }
    }
  }
}

@Composable
private fun BulletinPanel(isLandscape: Boolean, onClose: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(CockpitTokens.Bg.Parchment),
  ) {
    PanelHeader(onClose = onClose)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(CockpitTokens.Space.md),
    ) {
      if (isLandscape) {
        BulletinGrid()
      } else {
        BulletinStack()
      }
    }
  }
}

/** Portrait: 4 cards stacked, each fills equal weighted height. */
@Composable
private fun BulletinStack() {
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(CockpitTokens.Space.sm),
  ) {
    ELDERS.forEach { elder ->
      ClanBulletinCard(
        elder = elder,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
      )
    }
  }
}

/** Landscape: 2×2 grid, all cards equal-sized. */
@Composable
private fun BulletinGrid() {
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(CockpitTokens.Space.sm),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().weight(1f),
      horizontalArrangement = Arrangement.spacedBy(CockpitTokens.Space.sm),
    ) {
      ClanBulletinCard(elder = ELDERS[0], modifier = Modifier.weight(1f).fillMaxHeight())
      ClanBulletinCard(elder = ELDERS[1], modifier = Modifier.weight(1f).fillMaxHeight())
    }
    Row(
      modifier = Modifier.fillMaxWidth().weight(1f),
      horizontalArrangement = Arrangement.spacedBy(CockpitTokens.Space.sm),
    ) {
      ClanBulletinCard(elder = ELDERS[2], modifier = Modifier.weight(1f).fillMaxHeight())
      ClanBulletinCard(elder = ELDERS[3], modifier = Modifier.weight(1f).fillMaxHeight())
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
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = "PUBLIC BULLETIN BOARD",
      // Uses the same Cinzel typography as the cockpit's CLAN/WORLD title.
      style = TextStyle(
        fontFamily = CockpitFonts.Cinzel,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 3.36.sp, // 0.24em
        color = CockpitTokens.TextC.OnParchment,
      ),
    )
    Box(
      modifier = Modifier
        .clickable(onClick = onClose)
        .padding(horizontal = 8.dp, vertical = 2.dp),
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
private fun ClanBulletinCard(elder: Elder, modifier: Modifier = Modifier) {
  val accent = elder.accent
  val live = useBulletins(elder.clanId)
  val posts = when (live) {
    is QueryState.Live -> live.data.map { it.toPublicPost() }
    else -> if (BuildConfig.STUB_FALLBACK_ENABLED) StubData.publicBulletins(elder.clanId) else emptyList()
  }
  val currentTick = posts.maxOfOrNull { it.tick } ?: StubData.CURRENT_TICK
  val visible = posts.filter { (currentTick - it.tick) <= StubData.VISIBLE_TICKS }
  val old     = posts.filter { (currentTick - it.tick)  > StubData.VISIBLE_TICKS }

  Column(
    modifier = modifier
      .clip(RoundedCornerShape(CardCorner))
      .background(CardBg)
      .border(1.dp, CockpitTokens.Border.ParchmentEdge, RoundedCornerShape(CardCorner))
      .padding(horizontal = CockpitTokens.Space.md, vertical = CockpitTokens.Space.sm),
    verticalArrangement = Arrangement.spacedBy(CockpitTokens.Space.sm),
  ) {
    // Card header: clan glyph + name, accent underline (fixed at top)
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

    // Scrollable post list — fills remaining card height
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      visible.forEach { post ->
        BulletinCardRow(post = post, accent = accent, hidden = false)
      }
      if (old.isNotEmpty()) {
        OldSeparator()
        old.forEach { post ->
          BulletinCardRow(post = post, accent = accent, hidden = true)
        }
      }
    }
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
        letterSpacing = 1.44.sp,
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
