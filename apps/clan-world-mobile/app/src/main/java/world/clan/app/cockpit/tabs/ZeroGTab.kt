package world.clan.app.cockpit.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.clan.app.BuildConfig
import world.clan.app.cockpit.tabs.shared.SectionHeader
import world.clan.app.data.Elder
import world.clan.app.data.StubData
import world.clan.app.data.convex.QueryState
import world.clan.app.data.convex.toDomain
import world.clan.app.data.convex.useMemoryEvents
import world.clan.app.data.convex.useMemoryKv
import world.clan.app.ui.theme.CockpitFonts
import world.clan.app.ui.theme.CockpitTokens

private val WRITE_BROWN = Color(0xFF7A3A1A)

/**
 * 0G memory tab. Visual port of ZeroGTab.tsx — iNFT metadata, KV state,
 * and memory CRUD log.
 * Mono-heavy technical readout on parchment.
 */
@Composable
fun ZeroGTab(elder: Elder, modifier: Modifier = Modifier) {
  // iNFT metadata is still stub-only — there's no canonical Convex source
  // for it yet (would need 0G iNFT registry wiring). Live data flows in
  // for KV / CRUD; falls back to per-clan stubs when empty.
  val meta = StubData.inftMeta(elder.clanId)

  val kvState = useMemoryKv(elder.clanId)
  val crudState = useMemoryEvents(elder.clanId)

  val kvRows = when (kvState) {
    is QueryState.Live -> kvState.data.map { it.toDomain() }
    else -> if (BuildConfig.STUB_FALLBACK_ENABLED) StubData.kv(elder.clanId) else emptyList()
  }
  val crudRows = when (crudState) {
    is QueryState.Live -> crudState.data.map { it.toDomain() }
    else -> if (BuildConfig.STUB_FALLBACK_ENABLED) StubData.crud(elder.clanId) else emptyList()
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CockpitTokens.Bg.Parchment)
      .verticalScroll(rememberScrollState())
      .padding(CockpitTokens.Space.md),
    verticalArrangement = Arrangement.spacedBy(CockpitTokens.Space.md),
  ) {
    SectionHeader("iNFT METADATA")
    InftMetaGrid(meta)

    SectionHeader("KV STATE", trailing = "live")
    KvList(kvRows, accent = elder.accent)

    SectionHeader("MEMORY CRUD")
    CrudList(crudRows)
  }
}

@Composable
private fun InftMetaGrid(meta: StubData.InftMeta) {
  val pairs = listOf(
    "token_id"   to meta.tokenId,
    "owner"      to meta.owner,
    "archetype"  to meta.archetype,
    "state_root" to meta.stateRoot,
    "encrypted"  to (if (meta.encrypted) "true" else "false"),
    "version"    to meta.version,
  )
  Column(
    verticalArrangement = Arrangement.spacedBy(4.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    pairs.forEach { (k, v) ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = k,
          modifier = Modifier.width(96.dp),
          style = TextStyle(
            fontFamily = CockpitFonts.JetBrainsMono,
            fontSize = 10.sp,
            color = CockpitTokens.TextC.OnParchmentDim,
          ),
        )
        Text(
          text = v,
          modifier = Modifier.weight(1f),
          style = TextStyle(
            fontFamily = CockpitFonts.JetBrainsMono,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = CockpitTokens.TextC.OnParchment,
          ),
        )
      }
    }
  }
}

@Composable
private fun KvList(rows: List<StubData.KvRow>, accent: Color) {
  Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
    rows.forEach { kv ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color.White.copy(alpha = 0.18f))
          .padding(horizontal = 6.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = kv.key,
          style = TextStyle(
            fontFamily = CockpitFonts.JetBrainsMono,
            fontSize = 10.sp,
            color = CockpitTokens.TextC.OnParchmentDim,
          ),
        )
        Text(
          text = kv.value,
          style = TextStyle(
            fontFamily = CockpitFonts.JetBrainsMono,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = accent,
          ),
        )
      }
    }
  }
}

@Composable
private fun CrudList(rows: List<StubData.CrudRow>) {
  Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
    rows.forEach { row ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color.White.copy(alpha = 0.18f))
          .padding(horizontal = 6.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "T${row.tick.toString().padStart(2, '0')}",
          modifier = Modifier.width(28.dp),
          style = TextStyle(
            fontFamily = CockpitFonts.JetBrainsMono,
            fontSize = 10.sp,
            color = CockpitTokens.TextC.Muted,
          ),
        )
        Text(
          text = if (row.op == StubData.CrudOp.Write) "WRITE" else "READ",
          modifier = Modifier.width(52.dp),
          style = TextStyle(
            fontFamily = CockpitFonts.JetBrainsMono,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (row.op == StubData.CrudOp.Write) WRITE_BROWN else CockpitTokens.TextC.Muted,
          ),
        )
        Row(
          modifier = Modifier.weight(1f),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = row.key,
            style = TextStyle(
              fontFamily = CockpitFonts.JetBrainsMono,
              fontSize = 10.sp,
              color = CockpitTokens.TextC.OnParchmentDim,
            ),
          )
          if (row.note != null) {
            Text(
              text = "— ${row.note}",
              style = TextStyle(
                fontFamily = CockpitFonts.JetBrainsMono,
                fontSize = 10.sp,
                color = CockpitTokens.TextC.OnParchment,
              ),
            )
          }
        }
      }
    }
  }
}
