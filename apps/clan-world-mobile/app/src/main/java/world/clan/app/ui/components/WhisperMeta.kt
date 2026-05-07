package world.clan.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import world.clan.app.data.CombinedComm
import world.clan.app.viewmodel.clanDisplayName

/**
 * Build the meta line for a whisper / orch / human comm row. Used by
 * Hearth, WhispersScreen, and InftDetail's Whispers panel so the
 * formatting stays consistent across the three surfaces.
 *
 * Output is an `AnnotatedString` produced by [boldedMeta] so the
 * starred segments render bold against the rest.
 *
 * Examples:
 *   "*Tideborne* · whispered to · *Twilight* · 0042"
 *   "*Orchestrator* · 0042"
 *   "*Owner* · steered · 0042"
 */
@Composable
fun whisperMetaText(c: CombinedComm): AnnotatedString {
  val from = c.fromClan?.let { clanDisplayName(it) }
    ?: c.speaker
    ?: when (c.kind) {
      "orch" -> "Orchestrator"
      "human" -> "Owner"
      else -> "—"
    }
  val to = c.targetClan?.let { clanDisplayName(it) }
  val tickStr = c.tick?.let { "%04d".format(it) } ?: "—"
  val raw = when (c.kind) {
    "whisper" -> if (to != null) "*$from* · whispered to · *$to* · $tickStr"
                 else "*$from* · whispered · $tickStr"
    "orch" -> "*Orchestrator* · $tickStr"
    "human" -> "*$from* · steered · $tickStr"
    else -> "*$from* · $tickStr"
  }
  return boldedMeta(raw.uppercase())
}
