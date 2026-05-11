# ClanWorld GOLD Devnet Deploy

1. Generate or supply `keys/solana-devnet-deployer.json` from the repo root.
2. Fund it with Devnet SOL.
3. Replace the temporary program id in `Anchor.toml` and `src/lib.rs` with `anchor keys list`.
4. Run `anchor build && anchor deploy` from `packages/solana-gold`.
5. Derive PDA `mint-authority` for the deployed program, then create a classic SPL mint with `--decimals 0 --mint-authority <PDA>`.
6. Set Android/Convex env vars:
   - `CLAN_WORLD_GOLD_RPC_URL`
   - `CLAN_WORLD_GOLD_MINT`
   - `CLAN_WORLD_GOLD_FAUCET_PROGRAM_ID`
   - `CLAN_WORLD_GOLD_TREASURY_OWNER`
