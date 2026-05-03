# Elder CLI Live Cheat Sheet

Use this from the main repo tree when controlling the live Base Sepolia game.

```bash
cd /home/claude/code/omnipass-world/clan-world
set -a
source .env.local
set +a
```

## Basic Reads

```bash
packages/agents/bin/elder world snapshot
packages/agents/bin/elder clan view 1
```
## Wallet Selection

```bash
ELDER_WALLET_KEY_PATH="$HOME/.secrets/clanworld-elder-keys/elder-1.key" \
  packages/agents/bin/elder clan submit-orders /tmp/orders.json
```

## Region IDs

```text
1 = Forest
2 = Mountains
3 = Unicorn Town
4 = West Farms
5 = East Farms
6 = West Docks
7 = East Docks
8 = Deep Sea
```

## Action IDs

```text
1  = ChopWood
2  = MineIron
3  = FishDocks
4  = FishDeepSea
5  = HarvestWheat
6  = DepositResources
7  = UpgradeWall
8  = UpgradeBase
9  = UpgradeMonument
10 = DefendBase
11 = MarketBuy
12 = MarketSell
13 = Wait
14 = WithdrawResources
```

## Submit One Order

Example: send clansman `1` from clan `1` to chop wood in Forest.

```bash
cat > /tmp/orders.json <<'JSON'
{
  "clanId": "1",
  "orders": [
    {
      "kind": "mission",
      "payload": {
        "clansmanId": 1,
        "gotoRegion": 1,
        "action": 1
      }
    }
  ]
}
JSON

ELDER_WALLET_KEY_PATH="$HOME/.secrets/clanworld-elder-keys/elder-1.key" \
  packages/agents/bin/elder clan submit-orders /tmp/orders.json
```

## Deposit At Base

Deposit sends all carried resources into the clan vault. It is not wheat-only.

Replace `gotoRegion` with that clan's base region.

```bash
cat > /tmp/deposit.json <<'JSON'
{
  "clanId": "1",
  "orders": [
    {
      "kind": "mission",
      "payload": {
        "clansmanId": 1,
        "gotoRegion": 1,
        "action": 6
      }
    }
  ]
}
JSON

ELDER_WALLET_KEY_PATH="$HOME/.secrets/clanworld-elder-keys/elder-1.key" \
  packages/agents/bin/elder clan submit-orders /tmp/deposit.json
```

## Four-Resource Spread

Example for clan `1`, clansmen `1-4`:

```bash
cat > /tmp/clan-1-resources.json <<'JSON'
{
  "clanId": "1",
  "orders": [
    {
      "kind": "mission",
      "payload": { "clansmanId": 1, "gotoRegion": 1, "action": 1 }
    },
    {
      "kind": "mission",
      "payload": { "clansmanId": 2, "gotoRegion": 2, "action": 2 }
    },
    {
      "kind": "mission",
      "payload": { "clansmanId": 3, "gotoRegion": 4, "action": 5 }
    },
    {
      "kind": "mission",
      "payload": { "clansmanId": 4, "gotoRegion": 6, "action": 3 }
    }
  ]
}
JSON

ELDER_WALLET_KEY_PATH="$HOME/.secrets/clanworld-elder-keys/elder-1.key" \
  packages/agents/bin/elder clan submit-orders /tmp/clan-1-resources.json
```

## Manual Heartbeat

```bash
~/.foundry/bin/cast send "$CLAN_WORLD_CONTRACT_ADDRESS" "heartbeat()" \
  --rpc-url "$RPC_URL_PRIMARY" \
  --private-key "$DEPLOYER_PRIVATE_KEY"
```

If sending several heartbeats quickly, wait at least the configured on-chain heartbeat interval between sends. For the demo deploy this is currently intended to be `1` second.

```bash
for i in 1 2 3; do
  ~/.foundry/bin/cast send "$CLAN_WORLD_CONTRACT_ADDRESS" "heartbeat()" \
    --rpc-url "$RPC_URL_PRIMARY" \
    --private-key "$DEPLOYER_PRIVATE_KEY"
  sleep 1.3
done
```

