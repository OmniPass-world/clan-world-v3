package world.clan.app.wallet

import com.solana.mobilewalletadapter.clientlib.Blockchain
import com.solana.mobilewalletadapter.clientlib.Solana

/**
 * Canonical Solana cluster every MWA session in this app MUST request.
 *
 * Clan World is a devnet-only deployment (GOLD mint, faucet program, RPC
 * URL are all devnet). Authorizing against [Solana.Mainnet] anywhere —
 * even just for an Ælder sign-in message — primes the wallet's
 * remembered network to mainnet and surfaces a confusing
 * "Network mismatch (mainnet vs devnet)" dialog when the user then tries
 * to mint. Issue #229.
 *
 * Routing every site through this constant lets a single unit test
 * (`MwaClusterTest`) trap an accidental Mainnet swap.
 */
val ClanWorldMwaCluster: Blockchain = Solana.Devnet
