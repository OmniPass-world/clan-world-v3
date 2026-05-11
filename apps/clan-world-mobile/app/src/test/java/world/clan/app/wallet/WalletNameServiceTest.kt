package world.clan.app.wallet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WalletNameServiceTest {

  private val pubkey = "3f9fRjLaDSDVxd26xMEm4WuSXv62cGt5qVfEVGwMfTz6"

  @Test
  fun parsePrimarySolDomainAddsSuffix() {
    val body = """{"$pubkey":"11441"}"""

    assertEquals("11441.sol", WalletNameService.parsePrimarySolDomain(pubkey, body))
  }

  @Test
  fun parsePrimarySolDomainDoesNotDoubleSuffix() {
    val body = """{"$pubkey":"bonfida.sol"}"""

    assertEquals("bonfida.sol", WalletNameService.parsePrimarySolDomain(pubkey, body))
  }

  @Test
  fun parsePrimarySolDomainReturnsNullForMissingName() {
    assertNull(WalletNameService.parsePrimarySolDomain(pubkey, "{}"))
    assertNull(WalletNameService.parsePrimarySolDomain(pubkey, """{"other":"11441"}"""))
    assertNull(WalletNameService.parsePrimarySolDomain(pubkey, """{"$pubkey":""}"""))
    assertNull(WalletNameService.parsePrimarySolDomain(pubkey, """{"$pubkey":null}"""))
  }

  @Test
  fun parsePrimarySolDomainReturnsNullForMalformedJson() {
    assertNull(WalletNameService.parsePrimarySolDomain(pubkey, "not-json"))
  }
}
