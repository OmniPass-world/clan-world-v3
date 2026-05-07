package io.easya.kickstart

data class SparkPoint(
  val price: Double,
  val open: Double?,
  val high: Double?,
  val low: Double?,
  val close: Double?,
  val observedAt: Long,
)

data class KickstartToken(
  val tokenMint: String,
  val poolAddress: String,
  val name: String,
  val symbol: String,
  val iconUrl: String?,
  val usdPrice: Double,
  val mcap: Double,
  val liquidity: Double?,
  val volume24h: Double?,
  val priceChange1h: Double?,
  val priceChange6h: Double?,
  val priceChange24h: Double?,
  val priceChange7d: Double?,
  val rank: Int,
  val sparkline24h: List<SparkPoint> = emptyList(),
)
