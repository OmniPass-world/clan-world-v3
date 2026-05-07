package world.clan.gold

data class SparkPoint(
  val price: Double,
  val observedAt: Long,
)

data class GoldQuote(
  val usdPrice: Double,
  val priceChange1h: Double?,
  val priceChange6h: Double?,
  val priceChange24h: Double?,
  val priceChange7d: Double?,
  val fetchedAt: Long,
  val sparkline24h: List<SparkPoint>,
)
