package world.clan.app.data

/**
 * One row in the Bazaar marketplace. UI-demo data — no on-chain listing
 * mechanism exists yet, so listings are a static const list seeded from
 * the unlinked clan set (anything not in HearthViewModel.LINKED_CLAN_IDS).
 */
data class BazaarListing(
  val tokenId: Int,
  val clanId: Int,
  /** Hire price in whole gold per season. */
  val pricePerSeason: Int,
  /** Display-only owner short label (truncated pubkey). */
  val ownerShort: String,
  /** Hire-style availability blurb shown above the price. */
  val availability: String,
  /** Single-line flavor copy. */
  val pitch: String,
)

/**
 * Hardcoded listings for the slice 4 Bazaar demo. The five clan IDs not in
 * `HearthViewModel.LINKED_CLAN_IDS` (1, 3, 4, 7, 8) are listed.
 */
val BAZAAR_LISTINGS: List<BazaarListing> = listOf(
  BazaarListing(
    tokenId = 0x10A1,
    clanId = 1,
    pricePerSeason = 240,
    ownerShort = "9pK4 · 7vQy",
    availability = "available — i of i",
    pitch = "Mountain-bred. Reads winter as plainly as a name.",
  ),
  BazaarListing(
    tokenId = 0x10A3,
    clanId = 3,
    pricePerSeason = 320,
    ownerShort = "Hr8e · q2Fm",
    availability = "available — i of i",
    pitch = "Sun-keeper. Coin discipline is its first oath.",
  ),
  BazaarListing(
    tokenId = 0x10A4,
    clanId = 4,
    pricePerSeason = 180,
    ownerShort = "C4ka · M6sz",
    availability = "available — i of i",
    pitch = "Of the green country. Patient. Reads weather first.",
  ),
  BazaarListing(
    tokenId = 0x10A7,
    clanId = 7,
    pricePerSeason = 360,
    ownerShort = "B2nv · F1xz",
    availability = "available — i of i",
    pitch = "Forge-born. Steady iron over showy gold.",
  ),
  BazaarListing(
    tokenId = 0x10A8,
    clanId = 8,
    pricePerSeason = 280,
    ownerShort = "Tg9q · 4kPx",
    availability = "available — i of i",
    pitch = "Walks the longer line. Treats the season as a cipher.",
  ),
)

fun bazaarListingByClan(clanId: Int): BazaarListing? =
  BAZAAR_LISTINGS.firstOrNull { it.clanId == clanId }
