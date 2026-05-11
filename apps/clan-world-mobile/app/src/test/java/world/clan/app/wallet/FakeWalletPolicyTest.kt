package world.clan.app.wallet

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeWalletPolicyTest {
  @Test
  fun releaseBlocksFakeWalletUris() {
    assertTrue(
      FakeWalletPolicy.shouldBlock(
        allowFakeWallets = false,
        walletUriBase = "solana-wallet://com.solana.mobilewalletadapter.fakewallet",
      ),
    )
  }

  @Test
  fun releaseBlocksFakerWalletUriSpelling() {
    assertTrue(
      FakeWalletPolicy.shouldBlock(
        allowFakeWallets = false,
        walletUriBase = "solana-wallet://com.solana.mobilewalletadapter.fakerwallet",
      ),
    )
  }

  @Test
  fun releaseNormalizesWalletUriBeforeMatching() {
    assertTrue(
      FakeWalletPolicy.shouldBlock(
        allowFakeWallets = false,
        walletUriBase = "solana-wallet://Fake-Wallet",
      ),
    )
  }

  @Test
  fun releaseAllowsRealWalletMetadata() {
    assertFalse(
      FakeWalletPolicy.shouldBlock(
        allowFakeWallets = false,
        walletUriBase = "phantom://wallet",
      ),
    )
    assertFalse(
      FakeWalletPolicy.shouldBlock(
        allowFakeWallets = false,
        walletUriBase = "solflare://wallet",
      ),
    )
    assertFalse(
      FakeWalletPolicy.shouldBlock(
        allowFakeWallets = false,
        walletUriBase = null,
      ),
    )
  }

  @Test
  fun debugAllowsFakeWallets() {
    assertFalse(
      FakeWalletPolicy.shouldBlock(
        allowFakeWallets = true,
        walletUriBase = "solana-wallet://fakewallet",
      ),
    )
  }
}
