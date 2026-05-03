# Base Sepolia Deployment Runbook

## Diamond Deploy

The diamond deploy is large and sends many transactions. Always run the broadcast with `--slow` so Foundry waits for each transaction before submitting the next one:

```bash
cd packages/contracts
forge script script/Deploy.s.sol \
  --rpc-url "$RPC_URL_PRIMARY" \
  --private-key "$DEPLOYER_PRIVATE_KEY" \
  --broadcast \
  --slow
```

## Demo Heartbeat Cadence

For the live Base Sepolia demo, set the diamond heartbeat guard to `1` second after deploying or upgrading `HeartbeatFacet`:

```bash
cast send "$CLAN_WORLD_CONTRACT_ADDRESS" \
  "setHeartbeatIntervalSeconds(uint64)" 1 \
  --rpc-url "$RPC_URL_PRIMARY" \
  --private-key "$DEPLOYER_PRIVATE_KEY"

cast call "$CLAN_WORLD_CONTRACT_ADDRESS" \
  "heartbeatIntervalSeconds()(uint64)" \
  --rpc-url "$RPC_URL_PRIMARY"
```

The unset/default cadence remains `60` seconds. The `1` second setting is for manual test cycles and the hackathon demo only.
