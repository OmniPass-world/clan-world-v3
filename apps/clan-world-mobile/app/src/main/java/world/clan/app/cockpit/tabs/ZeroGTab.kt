package world.clan.app.cockpit.tabs

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.clan.app.cockpit.tabs.shared.SectionHeader
import world.clan.app.data.Elder
import world.clan.app.data.StubData
import world.clan.app.data.convex.QueryState
import world.clan.app.data.convex.toDomain
import world.clan.app.data.convex.useBulletins
import world.clan.app.data.convex.useMemoryEvents
import world.clan.app.data.convex.useMemoryKv
import world.clan.app.data.convex.useSnapshot
import world.clan.app.ui.theme.CockpitFonts
import world.clan.app.ui.theme.CockpitTokens

private val WRITE_BROWN = Color(0xFF7A3A1A)
private val ACCENT_TINT = Color(0x1AD4A544) // ~10% of accent for bulletin row bg

/**
 * 0G memory tab. Visual port of ZeroGTab.tsx — four sections: iNFT
 * metadata, KV state, memory CRUD log, and bulletin sightings.
 * Mono-heavy technical readout on parchment.
 */
@Composable
fun ZeroGTab(elder: Elder, modifier: Modifier = Modifier) {
  // iNFT metadata is still stub-only — there's no canonical Convex source
  // for it yet (would need 0G iNFT registry wiring). Live data flows in
  // for KV / CRUD / bulletins; falls back to per-clan stubs when empty.
  val meta = StubData.inftMeta(elder.clanId)

  val snapshotState = useSnapshot()
  val kvState = useMemoryKv(elder.clanId)
  val crudState = useMemoryEvents(elder.clanId)
  val bulletinState = useBulletins(elder.clanId)

  // Anchor bulletin age to the live world tick instead of max(bulletin.slot).
  // Old formula reported "0t" when the only bulletin was 100 ticks stale,
  // because max == itself. Falls back to the stub's CURRENT_TICK if snapshot
  // isn't live yet — matches the precedent in VaultTab.
  val currentTick = (snapshotState as? QueryState.Live)?.data?.tick?.toInt()
    ?: StubData.CURRENT_TICK

  val kvRows = when (kvState) {
    is QueryState.Live -> kvState.data.map { it.toDomain() }
    else -> StubData.kv(elder.clanId)
  }
  val crudRows = when (crudState) {
    is QueryState.Live -> crudState.data.map { it.toDomain() }
    else -> StubData.crud(elder.clanId)
  }
  val bulletins = when (bulletinState) {
    is QueryState.Live -> bulletinState.data.map { it.toDomain(currentSlot = currentTick) }
    else -> StubData.bulletinsForOwner(elder.clanId)
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

    SectionHeader("BULLETINS")
    BulletinList(bulletins)
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

@Composable
private fun BulletinList(rows: List<StubData.BulletinRow>) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    rows.forEach { b ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(IntrinsicSize.Min)
          .background(ACCENT_TINT),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier
            .width(2.dp)
            .fillMaxHeight()
            .background(CockpitTokens.TextC.Accent),
        )
        Row(
          modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = b.body,
            modifier = Modifier.weight(1f),
            style = TextStyle(
              fontFamily = CockpitFonts.Inter,
              fontSize = 11.sp,
              fontStyle = FontStyle.Italic,
              color = CockpitTokens.TextC.OnParchment,
            ),
          )
          Text(
            text = b.age,
            style = TextStyle(
              fontFamily = CockpitFonts.JetBrainsMono,
              fontSize = 9.sp,
              color = CockpitTokens.TextC.Muted,
            ),
          )
        }
      }
    }
  }
}
