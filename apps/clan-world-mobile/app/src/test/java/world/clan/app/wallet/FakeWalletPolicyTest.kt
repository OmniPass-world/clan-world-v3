package world.clan.app.wallet

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeWalletPolicyTest {
  @Test
  fun releaseBlocksFakeWalletLabels() {
    assertTrue(
      FakeWalletPolicy.shouldBlock(
        allowFakeWallets = false,
        walletUriBase = null,
        walletLabel = "Fake Wallet app",
      ),
    )
  }

  @Test
  fun releaseBlocksFakeWalletUris() {
    assertTrue(
      FakeWalletPolicy.shouldBlock(
        allowFakeWallets = false,
        walletUriBase = "solana-wallet://com.solana.mobilewalletadapter.fakewallet",
        walletLabel = "Test account",
      ),
    )
  }

  @Test
  fun releaseBlocksFakerWalletSpelling() {
    assertTrue(
      FakeWalletPolicy.shouldBlock(
        allowFakeWallets = false,
        walletUriBase = null,
        walletLabel = "Faker Wallet",
      ),
    )
  }

  @Test
  fun releaseAllowsRealWalletMetadata() {
    assertFalse(
      FakeWalletPolicy.shouldBlock(
        allowFakeWallets = false,
        walletUriBase = "phantom://wallet",
        walletLabel = "Phantom",
      ),
    )
    assertFalse(
      FakeWalletPolicy.shouldBlock(
        allowFakeWallets = false,
        walletUriBase = "solflare://wallet",
        walletLabel = "Solflare",
      ),
    )
  }

  @Test
  fun debugAllowsFakeWallets() {
    assertFalse(
      FakeWalletPolicy.shouldBlock(
        allowFakeWallets = true,
        walletUriBase = "solana-wallet://fakewallet",
        walletLabel = "Fake Wallet",
      ),
    )
  }
}
