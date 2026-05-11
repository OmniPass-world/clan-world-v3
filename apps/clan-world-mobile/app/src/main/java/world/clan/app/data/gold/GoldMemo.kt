package world.clan.app.data.gold

import java.security.MessageDigest

object GoldMemo {
  const val DOCTRINE_SEPARATOR = "\n---notes---\n"

  fun sha256Hex(text: String): String =
    MessageDigest.getInstance("SHA-256")
      .digest(text.toByteArray(Charsets.UTF_8))
      .joinToString("") { "%02x".format(it) }

  fun whisper(
    clanId: Int,
    body: String,
    owner: String,
    burnAmount: Long = GoldSolanaClient.BURN_AMOUNT,
    skipTax: Long,
  ): String =
    "clanworld:whisper:v1:$clanId:${sha256Hex(body)}:$owner:$burnAmount:$skipTax"

  fun doctrine(
    clanId: Int,
    strategy: String,
    notes: String,
    owner: String,
    burnAmount: Long = GoldSolanaClient.BURN_AMOUNT,
  ): String =
    "clanworld:doctrine:v1:$clanId:${sha256Hex(strategy + DOCTRINE_SEPARATOR + notes)}:$owner:$burnAmount:0"

  fun faucet(owner: String, amount: Long = GoldSolanaClient.FAUCET_AMOUNT): String =
    "clanworld:faucet:v1:$owner:$amount"
}
