package world.clan.app.cockpit.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.clan.app.BuildConfig
import world.clan.app.data.Elder
import world.clan.app.data.StubData
import world.clan.app.data.convex.QueryState
import world.clan.app.data.convex.toDomain
import world.clan.app.data.convex.toPublicPost
import world.clan.app.data.convex.useBulletins
import world.clan.app.data.convex.useCombinedComms
import world.clan.app.data.elderById
import world.clan.app.data.fadeOpacityForTick
import world.clan.app.ui.theme.CockpitFonts
import world.clan.app.ui.theme.CockpitTokens

private val ORCH_GREY     = Color(0xFF5A5A5A)
private val HUMAN_BORDER  = Color(0xFFB8862E)
private val HUMAN_FG      = Color(0xFF7A4E10)

private val SPEAKER_CLAN_REGEX = Regex("^clan-(\\d+)$")

private enum class CommsView { Axl, Bulletin }

/**
 * Comms tab. Visual port of CommsTab.tsx — a private (AXL) feed of
 * whispers/orch/human messages with a recipients chip set, plus a
 * public bulletin board view (toggle).
 *
 * Older messages fade per `fadeOpacityForTick` (mirrors the web).
 */
@Composable
fun CommsTab(elder: Elder, modifier: Modifier = Modifier) {
  var view by rememberSaveable(elder.clanId) { mutableStateOf(CommsView.Axl) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CockpitTokens.Bg.Parchment)
      .padding(CockpitTokens.Space.md),
    verticalArrangement = Arrangement.spacedBy(CockpitTokens.Space.sm),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.Bottom,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = "COMMS",
        style = TextStyle(
          fontFamily = CockpitFonts.Cinzel,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = CockpitTokens.TextC.OnParchmentDim,
          letterSpacing = 1.98.sp,
        ),
      )
      ViewToggle(view = view, onChange = { view = it })
    }
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(CockpitTokens.Border.ParchmentEdge),
    )

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(if (view == CommsView.Axl) 6.dp else 4.dp),
    ) {
      when (view) {
        CommsView.Axl      -> AxlList(elder)
        CommsView.Bulletin -> BulletinView(elder)
      }
    }
  }
}

@Composable
private fun ViewToggle(view: CommsView, onChange: (CommsView) -> Unit) {
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(CockpitTokens.Radius.sm))
      .border(1.dp, CockpitTokens.Border.ParchmentEdge, RoundedCornerShape(CockpitTokens.Radius.sm)),
  ) {
    ToggleButton(label = "PRIVATE", sub = "encrypted", active = view == CommsView.Axl,      onClick = { onChange(CommsView.Axl) })
    ToggleButton(label = "PUBLIC",  sub = "bulletin",  active = view == CommsView.Bulletin, onClick = { onChange(CommsView.Bulletin) })
  }
}

@Composable
private fun ToggleButton(label: String, sub: String, active: Boolean, onClick: () -> Unit) {
  val bg = if (active) CockpitTokens.TextC.Accent else Color.Transparent
  val fg = if (active) CockpitTokens.Bg.Ink else CockpitTokens.TextC.OnParchmentDim
  Column(
    modifier = Modifier
      .widthIn(min = 70.dp)
      .background(bg)
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp, vertical = 3.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = label,
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = fg,
        letterSpacing = 0.8.sp,
      ),
    )
    Text(
      text = sub.uppercase(),
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 8.sp,
        color = fg.copy(alpha = 0.7f),
        letterSpacing = 0.96.sp, // 0.12em
      ),
    )
  }
}

@Composable
private fun AxlList(elder: Elder) {
  val live = useCombinedComms(elder.clanId)
  when (live) {
    is QueryState.Live -> {
      val lines = live.data.map { it.toDomain() }
      if (lines.isEmpty()) {
        EmptyCommsState("no live private comms yet")
      } else {
        lines.forEach { line ->
          CommsBubble(line, myClanId = elder.clanId)
        }
      }
    }
    QueryState.Loading -> {
      if (BuildConfig.STUB_FALLBACK_ENABLED) {
        StubData.commsLines(elder.clanId).forEach { line ->
          CommsBubble(line, myClanId = elder.clanId)
        }
      } else {
        EmptyCommsState("loading private comms...")
      }
    }
    QueryState.Stub -> {
      if (BuildConfig.STUB_FALLBACK_ENABLED) {
        StubData.commsLines(elder.clanId).forEach { line ->
          CommsBubble(line, myClanId = elder.clanId)
        }
      } else {
        EmptyCommsState("no live private comms yet")
      }
    }
    is QueryState.Error -> EmptyCommsState("private comms unavailable")
  }
}

@Composable
private fun CommsBubble(line: StubData.CommsLine, myClanId: Int) {
  val (label, bg, border, fg) = bubbleStyle(line)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(IntrinsicSize.Min)
      .alpha(fadeOpacityForTick(line.tick))
      .background(bg),
  ) {
    Box(
      modifier = Modifier
        .width(3.dp)
        .fillMaxHeight()
        .background(border),
    )
    Column(
      modifier = Modifier
        .padding(horizontal = 8.dp, vertical = 6.dp)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
      // Header row — three equal columns so the label can sit either on
      // the left (whisper) or centred (orch/human) without affecting the
      // tick's right alignment. Mirrors the web's 3-col `1fr auto 1fr`.
      val labelStyle = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = fg,
        letterSpacing = 1.08.sp, // 0.12em
      )
      val tickStyle = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 9.sp,
        color = CockpitTokens.TextC.Muted,
      )
      val tickText = "T${line.tick.toString().padStart(2, '0')}"
      val isWhisper = line.kind == StubData.CommsKind.Whisper
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier.weight(1f),
          contentAlignment = Alignment.CenterStart,
        ) {
          if (isWhisper) Text(text = labelLine(line, label), style = labelStyle)
        }
        Box(
          modifier = Modifier.weight(1f),
          contentAlignment = Alignment.Center,
        ) {
          if (!isWhisper) Text(text = labelLine(line, label), style = labelStyle)
        }
        Box(
          modifier = Modifier.weight(1f),
          contentAlignment = Alignment.CenterEnd,
        ) {
          Text(text = tickText, style = tickStyle)
        }
      }

      // Recipients chips (only for self-whispers w/ recipients)
      if (line.kind == StubData.CommsKind.Whisper &&
          line.recipients.isNotEmpty() &&
          speakerToClanId(line.speaker) == myClanId) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(3.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "sent to:",
            style = TextStyle(
              fontFamily = CockpitFonts.JetBrainsMono,
              fontSize = 8.sp,
              fontWeight = FontWeight.SemiBold,
              color = CockpitTokens.TextC.OnParchmentDim,
            ),
          )
          line.recipients.forEach { rcptClanId ->
            RecipientChip(clanId = rcptClanId)
          }
        }
      }

      // Body
      Text(
        text = line.body,
        modifier = Modifier.fillMaxWidth(),
        style = TextStyle(
          fontFamily = CockpitFonts.Inter,
          fontSize = 11.sp,
          fontStyle = if (line.kind == StubData.CommsKind.Human) FontStyle.Italic else FontStyle.Normal,
          color = CockpitTokens.TextC.OnParchment,
          textAlign = if (line.kind == StubData.CommsKind.Whisper) TextAlign.Start else TextAlign.Center,
        ),
      )
    }
  }
}

@Composable
private fun RecipientChip(clanId: Int) {
  val accent = elderById(clanId).accent.copy(alpha = 0.85f)
  Box(
    modifier = Modifier
      .height(11.dp)
      .widthIn(min = 11.dp)
      .clip(CircleShape)
      .background(accent)
      .padding(horizontal = 3.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = clanId.toString(),
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 7.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
      ),
    )
  }
}

private data class BubbleStyle(val label: String, val bg: Color, val border: Color, val fg: Color)

private fun bubbleStyle(line: StubData.CommsLine): BubbleStyle = when (line.kind) {
  StubData.CommsKind.Orch  -> BubbleStyle(
    label  = "ORCH",
    bg     = Color(0x1A8A8A8A),
    border = ORCH_GREY,
    fg     = ORCH_GREY,
  )
  StubData.CommsKind.Human -> BubbleStyle(
    label  = "HUMAN",
    bg     = Color(0x24D4A544),
    border = HUMAN_BORDER,
    fg     = HUMAN_FG,
  )
  StubData.CommsKind.Whisper -> {
    val sid = speakerToClanId(line.speaker)
    val accent = sid?.let { elderById(it).accent } ?: Color(0xFF5A8AA8)
    BubbleStyle(
      label  = "WHISPER",
      bg     = accent.copy(alpha = 0.12f),
      border = accent,
      fg     = accent,
    )
  }
}

private fun speakerToClanId(speaker: String): Int? {
  val m = SPEAKER_CLAN_REGEX.matchEntire(speaker) ?: return null
  return m.groupValues[1].toIntOrNull()
}

private fun labelLine(line: StubData.CommsLine, label: String): String =
  when (line.kind) {
    StubData.CommsKind.Whisper -> "[$label] ${line.speaker}"
    StubData.CommsKind.Human   -> "[$label] iNFT Owner"
    StubData.CommsKind.Orch    -> "[$label] orchestrator"
  }

@Composable
private fun BulletinView(elder: Elder) {
  val live = useBulletins(elder.clanId)
  val posts = when (live) {
    is QueryState.Live -> live.data.map { it.toPublicPost() }
    QueryState.Loading -> {
      if (BuildConfig.STUB_FALLBACK_ENABLED) {
        StubData.publicBulletins(elder.clanId)
      } else {
        EmptyCommsState("loading public bulletins...")
        return
      }
    }
    QueryState.Stub -> {
      if (BuildConfig.STUB_FALLBACK_ENABLED) {
        StubData.publicBulletins(elder.clanId)
      } else {
        EmptyCommsState("no live public bulletins yet")
        return
      }
    }
    is QueryState.Error -> {
      EmptyCommsState("public bulletins unavailable")
      return
    }
  }
  if (posts.isEmpty()) {
    EmptyCommsState("no live public bulletins yet")
    return
  }
  val currentTick = posts.maxOfOrNull { it.tick } ?: StubData.CURRENT_TICK
  val visible = posts.filter { (currentTick - it.tick) <= StubData.VISIBLE_TICKS }
  val old     = posts.filter { (currentTick - it.tick)  > StubData.VISIBLE_TICKS }

  visible.forEach { p -> BulletinPostRow(p, elder.accent, hidden = false) }
  if (old.isNotEmpty()) {
    OldBulletinSeparator()
    old.forEach { p -> BulletinPostRow(p, elder.accent, hidden = true) }
  }
}

@Composable
private fun BulletinPostRow(post: StubData.BulletinPost, accent: Color, hidden: Boolean) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(IntrinsicSize.Min)
      .alpha(fadeOpacityForTick(post.tick))
      .background(accent.copy(alpha = 0.10f))
      .border(1.dp, CockpitTokens.Border.ParchmentEdge),
  ) {
    Box(
      modifier = Modifier
        .width(3.dp)
        .fillMaxHeight()
        .background(accent),
    )
    Column(
      modifier = Modifier
        .padding(horizontal = 9.dp, vertical = 7.dp)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = post.speaker,
          style = TextStyle(
            fontFamily = CockpitFonts.JetBrainsMono,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = accent,
            letterSpacing = 1.08.sp,
          ),
        )
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          VisibilityChip(hidden = hidden)
          Text(
            text = "T${post.tick.toString().padStart(2, '0')}",
            style = TextStyle(
              fontFamily = CockpitFonts.JetBrainsMono,
              fontSize = 9.sp,
              color = CockpitTokens.TextC.Muted,
            ),
          )
        }
      }
      Text(
        text = post.body,
        style = TextStyle(
          fontFamily = CockpitFonts.Inter,
          fontSize = 11.sp,
          color = CockpitTokens.TextC.OnParchment,
        ),
      )
    }
  }
}

@Composable
private fun EmptyCommsState(message: String) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(Color.White.copy(alpha = 0.18f))
      .border(1.dp, CockpitTokens.Border.ParchmentEdge)
      .padding(horizontal = 10.dp, vertical = 12.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = message,
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 10.sp,
        color = CockpitTokens.TextC.OnParchmentDim,
      ),
      textAlign = TextAlign.Center,
    )
  }
}

@Composable
private fun VisibilityChip(hidden: Boolean) {
  val (bg, fg, border, label) = if (hidden) {
    Quad(
      first  = Color(0x2D786C54),
      second = CockpitTokens.TextC.Muted,
      third  = CockpitTokens.Border.ParchmentEdge,
      fourth = "OLD",
    )
  } else {
    Quad(
      first  = Color(0x2E6AA888),
      second = Color(0xFF3A6A4A),
      third  = Color(0xFF6AA888),
      fourth = "VISIBLE",
    )
  }
  Box(
    modifier = Modifier
      .clip(CircleShape)
      .background(bg)
      .border(1.dp, border, CircleShape)
      .padding(horizontal = 6.dp, vertical = 1.dp),
  ) {
    Text(
      text = label,
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 8.sp,
        fontWeight = FontWeight.Bold,
        color = fg,
        letterSpacing = 0.64.sp,
      ),
    )
  }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun OldBulletinSeparator() {
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
      text = "OLD BULLETIN MESSAGES",
      style = TextStyle(
        fontFamily = CockpitFonts.JetBrainsMono,
        fontSize = 9.sp,
        color = CockpitTokens.TextC.Muted,
        letterSpacing = 1.62.sp, // 0.18em ≈ 9 * 0.18
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
