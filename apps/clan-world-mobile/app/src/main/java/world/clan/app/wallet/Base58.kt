package world.clan.app.wallet

/**
 * Bitcoin/Solana Base58 encoder. Lifted from a standard implementation;
 * keeps us independent of the web3-solana artifact's exact API surface,
 * which has churned across releases.
 */
object Base58 {
  private const val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"

  fun encode(input: ByteArray): String {
    if (input.isEmpty()) return ""
    // Count leading zeros.
    var zeros = 0
    while (zeros < input.size && input[zeros].toInt() == 0) zeros++
    // Convert base-256 to base-58 in a working buffer.
    val src = input.copyOf()
    val encoded = CharArray(src.size * 2) // upper bound
    var outIdx = encoded.size
    var startAt = zeros
    while (startAt < src.size) {
      val mod = divmod58(src, startAt)
      if (src[startAt].toInt() == 0) startAt++
      encoded[--outIdx] = ALPHABET[mod.toInt()]
    }
    while (outIdx < encoded.size && encoded[outIdx] == ALPHABET[0]) outIdx++
    repeat(zeros) { encoded[--outIdx] = ALPHABET[0] }
    return String(encoded, outIdx, encoded.size - outIdx)
  }

  fun decode(value: String): ByteArray {
    if (value.isEmpty()) return byteArrayOf()
    var bytes = byteArrayOf(0)
    value.forEach { c ->
      val digit = ALPHABET.indexOf(c)
      require(digit >= 0) { "Invalid base58 character: $c" }
      var carry = digit
      for (i in bytes.indices.reversed()) {
        carry += (bytes[i].toInt() and 0xff) * 58
        bytes[i] = carry.toByte()
        carry = carry ushr 8
      }
      while (carry > 0) {
        bytes = byteArrayOf((carry and 0xff).toByte()) + bytes
        carry = carry ushr 8
      }
    }
    val leadingZeros = value.takeWhile { it == ALPHABET[0] }.length
    return ByteArray(leadingZeros) + bytes.dropWhile { it.toInt() == 0 }.toByteArray()
  }

  /** Divides number (in `number` from `firstDigit` onwards) by 58, returns the remainder. */
  private fun divmod58(number: ByteArray, firstDigit: Int): Byte {
    var remainder = 0
    for (i in firstDigit until number.size) {
      val digit256 = number[i].toInt() and 0xFF
      val temp = remainder * 256 + digit256
      number[i] = (temp / 58).toByte()
      remainder = temp % 58
    }
    return remainder.toByte()
  }
}
