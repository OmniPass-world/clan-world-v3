package world.clan.app.wallet

import com.solana.mobilewalletadapter.clientlib.Solana
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Regression test for issue #229 Bug B: the MWA session MUST request the
 * `solana:devnet` cluster so wallets that auto-switch (or surface a switch
 * prompt) do so against the correct network instead of producing a raw
 * "Network mismatch — mainnet vs devnet" dialog mid-mint.
 *
 * Pinning [ClanWorldMwaCluster] (which both [world.clan.app.wallet.MwaClient]
 * and [world.clan.app.owner.Mwa] read from) traps any accidental
 * Mainnet/Testnet swap. Pinning the underlying SDK identifiers also traps
 * an SDK upgrade that renames the constants.
 */
class MwaClusterTest {

  @Test
  fun clanWorldClusterIsDevnet() {
    // The single source of truth every MWA call site uses. If someone ever
    // points this at Solana.Mainnet again, mint flow regresses to the
    // network-mismatch dialog Liam reported in v2.4.0.
    assertSame(Solana.Devnet, ClanWorldMwaCluster)
    assertEquals("solana", ClanWorldMwaCluster.name)
    assertEquals("devnet", ClanWorldMwaCluster.cluster)
  }

  @Test
  fun solanaMainnetIsNotDevnet() {
    // Sanity guard against an SDK rename that could make Mainnet alias
    // back onto Devnet (extremely unlikely but cheap to assert).
    assertEquals("mainnet", Solana.Mainnet.cluster)
  }
}
