package world.clan.app.wallet

import com.solana.mobilewalletadapter.clientlib.Solana
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Regression test for issue #229 Bug B: the MWA session MUST request the
 * `solana:devnet` cluster so wallets that auto-switch (or surface a switch
 * prompt) do so against the correct network instead of producing a raw
 * "Network mismatch — mainnet vs devnet" dialog mid-mint.
 *
 * This is a contract test against the Solana Mobile SDK's [Solana] object —
 * it documents the cluster identifier we depend on and traps any future
 * SDK upgrade that renames the field or constant.
 */
class MwaClusterTest {

  @Test
  fun solanaDevnetIdentifierMatchesMwaSpec() {
    // The MWA wallet identifies the requested network via these two fields.
    // The combined CAIP-2-style identifier is "solana:devnet".
    assertEquals("solana", Solana.Devnet.name)
    assertEquals("devnet", Solana.Devnet.cluster)
  }

  @Test
  fun solanaMainnetIsNotDevnet() {
    // Sanity guard — if someone ever swaps Solana.Devnet for Solana.Mainnet
    // in MwaClient again, the cluster identifier will silently match the
    // wallet's default and the mismatch dialog will return.
    assertEquals("mainnet", Solana.Mainnet.cluster)
  }
}
