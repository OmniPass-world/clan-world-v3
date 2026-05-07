package world.clan.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * The named text-style palette for Clan World mobile. Each style maps to
 * a specific CSS rule in `apps/clan-world-mobile/design/slice-1-prototype.html`
 * and is annotated with the prototype selector + line range that uses it.
 */
@Immutable
data class ClanWorldTypography(

  // ── Mono labels & micro text ──────────────────────────────────────────

  /** prototype: .eyebrow, .frame-num — 11sp, 0.32em uppercase */
  val eyebrow: TextStyle = TextStyle(
    fontFamily = JetBrainsMono, fontWeight = FontWeight.Normal,
    fontSize = 11.sp, letterSpacing = 0.32.em,
  ),

  /** prototype: .canvas-meta, .crown-right, .row-card .k — 10sp warm-faint key labels */
  val monoMicro: TextStyle = TextStyle(
    fontFamily = JetBrainsMono, fontWeight = FontWeight.Normal,
    fontSize = 10.sp, letterSpacing = 0.32.em,
  ),

  /** prototype: .lb-row .delta, .progress-marks, .stat .l — 9sp tightest mono */
  val monoNano: TextStyle = TextStyle(
    fontFamily = JetBrainsMono, fontWeight = FontWeight.Normal,
    fontSize = 9.sp, letterSpacing = 0.18.em,
  ),

  // ── Display (Cinzel) ──────────────────────────────────────────────────

  /** prototype: .canvas-title — 48sp page chrome (unused inside screens) */
  val displayLarge: TextStyle = TextStyle(
    fontFamily = Cinzel, fontWeight = FontWeight.Medium,
    fontSize = 48.sp, letterSpacing = 0.03.em, lineHeight = 50.sp,
  ),

  /** prototype: .wordmark, .h-title, .codex-title — 22sp 0.18em uppercase. Screen titles. */
  val displayHero: TextStyle = TextStyle(
    fontFamily = Cinzel, fontWeight = FontWeight.Medium,
    fontSize = 22.sp, letterSpacing = 0.18.em,
  ),

  /** prototype: .frame-name (mockup-page chrome — unused in app); kept for symmetry */
  val displaySerif: TextStyle = TextStyle(
    fontFamily = Cinzel, fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp, letterSpacing = 0.06.em,
  ),

  /** prototype: .crown-name, .device-chip .label — 13sp Cinzel 0.32em uppercase header chrome */
  val crownLabel: TextStyle = TextStyle(
    fontFamily = Cinzel, fontWeight = FontWeight.Medium,
    fontSize = 13.sp, letterSpacing = 0.32.em,
  ),

  /** prototype: .section-head .title, .codex-section h3 — 11sp Cinzel small-caps gold */
  val sectionHead: TextStyle = TextStyle(
    fontFamily = Cinzel, fontWeight = FontWeight.Medium,
    fontSize = 11.sp, letterSpacing = 0.36.em,
  ),

  /** prototype: .ember-cta, .tabs .t — Cinzel uppercase 14sp / 10sp (size override per use) */
  val ctaLabel: TextStyle = TextStyle(
    fontFamily = Cinzel, fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp, letterSpacing = 0.42.em,
  ),

  /** prototype: .letter-name (Hall card title) — 18sp Cinzel SemiBold on parchment */
  val letterName: TextStyle = TextStyle(
    fontFamily = Cinzel, fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp, letterSpacing = 0.06.em,
  ),

  /** prototype: .hero-name (iNFT detail hero) — 26sp Cinzel SemiBold on parchment */
  val heroName: TextStyle = TextStyle(
    fontFamily = Cinzel, fontWeight = FontWeight.SemiBold,
    fontSize = 26.sp, letterSpacing = 0.04.em, lineHeight = 28.sp,
  ),

  // ── Body (EB Garamond) ────────────────────────────────────────────────

  /** prototype: .canvas-deck, .lb-row .clan — 14sp EB Garamond body */
  val body: TextStyle = TextStyle(
    fontFamily = EBGaramond, fontWeight = FontWeight.Normal,
    fontSize = 14.sp, lineHeight = 22.sp, letterSpacing = 0.04.em,
  ),

  /** Larger reading body (page chrome). */
  val bodyLarge: TextStyle = TextStyle(
    fontFamily = EBGaramond, fontWeight = FontWeight.Normal,
    fontSize = 16.sp, lineHeight = 26.sp,
  ),

  // ── Script (Cormorant Italic) — the poetic voice ──────────────────────

  /** prototype: .frame-note, .whisper-body, .letter-meta, .mem-row .v, .codex-deck, .h-sub —
   *  14sp Cormorant Italic Medium, used everywhere mood text appears. */
  val scriptItalic: TextStyle = TextStyle(
    fontFamily = CormorantItalic, fontWeight = FontWeight.Medium,
    fontStyle = FontStyle.Italic,
    fontSize = 14.sp, lineHeight = 20.sp,
  ),

  /** prototype: .invocation — 18sp variant for the Connect screen poem */
  val scriptInvocation: TextStyle = TextStyle(
    fontFamily = CormorantItalic, fontWeight = FontWeight.Medium,
    fontStyle = FontStyle.Italic,
    fontSize = 18.sp, lineHeight = 27.sp,
  ),

  /** prototype: .h-sub, .seal trailer — smaller italic script (12sp) */
  val scriptItalicSmall: TextStyle = TextStyle(
    fontFamily = CormorantItalic, fontWeight = FontWeight.Medium,
    fontStyle = FontStyle.Italic,
    fontSize = 12.sp, lineHeight = 16.sp,
  ),

  // ── Numerics (JetBrains Mono) ─────────────────────────────────────────

  /** prototype: .tick-number — 48sp Light JetBrainsMono, the Hearth banner numeric */
  val tickNumber: TextStyle = TextStyle(
    fontFamily = JetBrainsMono, fontWeight = FontWeight.Light,
    fontSize = 48.sp, letterSpacing = 0.06.em, lineHeight = 48.sp,
  ),

  /** prototype: .tick-number .dim — 24sp partner to tickNumber */
  val tickNumberDim: TextStyle = TextStyle(
    fontFamily = JetBrainsMono, fontWeight = FontWeight.Light,
    fontSize = 24.sp, letterSpacing = 0.06.em,
  ),

  /** prototype: .lb-row .gold, .row-card .v — 13sp mono numeric */
  val monoData: TextStyle = TextStyle(
    fontFamily = JetBrainsMono, fontWeight = FontWeight.Normal,
    fontSize = 13.sp, letterSpacing = 0.04.em,
  ),

  /** prototype: .h-count b — 22sp Light mono "of viii" big numeral */
  val monoBig: TextStyle = TextStyle(
    fontFamily = JetBrainsMono, fontWeight = FontWeight.Light,
    fontSize = 22.sp, letterSpacing = 0.05.em,
  ),

  // ── Rune (Uncial Antiqua) ─────────────────────────────────────────────

  /** prototype: .hero-stamp — 13sp Uncial Antiqua "sealed tick" stamp */
  val runeLabel: TextStyle = TextStyle(
    fontFamily = UncialAntiqua, fontWeight = FontWeight.Normal,
    fontSize = 13.sp, letterSpacing = 0.32.em,
  ),
)

val LocalClanWorldTypography = staticCompositionLocalOf { ClanWorldTypography() }
