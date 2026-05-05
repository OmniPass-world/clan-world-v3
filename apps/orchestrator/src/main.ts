import { REGION_FOREST, type ClanOrder } from '@clan-world/shared';
import { createChainClient, createConvexClient } from '@clan-world/shared/adapters';
import { ActionType } from '@clan-world/shared/generated/enums';

const CLAN_ID = process.env.CLAN_ID || '1';

async function main(): Promise<void> {
  const chain = createChainClient();
  const convex = createConvexClient();

  // Get current tick
  const tick = await chain.getCurrentTick();
  console.error(`[orchestrator] tick=${tick}`);

  // Hardcoded mission for clan-1: clansman 1 chops wood in Forest
  const orders: ClanOrder[] = [
    {
      kind: 'mission',
      payload: { clansmanId: 1, gotoRegion: REGION_FOREST, action: ActionType.ChopWood },
    },
  ];

  console.error(`[orchestrator] submitting orders for clan-${CLAN_ID}...`);
  const { txHash, results: simulationResults } = await chain.submitOrders(CLAN_ID, orders);
  const failedSimulationResults = simulationResults.filter(result => result.status !== 0);
  console.error(`[orchestrator] tx submitted: ${txHash}; [sim] results=${JSON.stringify(simulationResults)}`);
  if (failedSimulationResults.length > 0) {
    console.error(`[orchestrator] [sim] ${failedSimulationResults.length} order(s) returned non-OK status`);
  }

  try {
    await convex.postLog(
      failedSimulationResults.length > 0 ? 'warn' : 'info',
      `Elder Aldric (clan-${CLAN_ID}): ChopWood submitted — txHash=${txHash} tick=${tick} [sim] results=${JSON.stringify(simulationResults)}`,
    );
  } catch (err) {
    console.error('[orchestrator] convex log failed (non-fatal):', err);
  }

  // Cockpit Comms feed — orchestrator-emitted "narration" each tick so the
  // ORCH bubbles populate. World-events (e.g. bandit camp surfacing) should
  // be a separate post() call from whichever subsystem emits them.
  await convex.postOrchEvent({
    tick,
    kind: 'narration',
    body: `Tick T${String(tick).padStart(2, '0')} begun. Orders submitted for clan-${CLAN_ID} (${
      failedSimulationResults.length === 0 ? 'OK' : `${failedSimulationResults.length} sim warning(s)`
    }).`,
    targetClanId: Number(CLAN_ID),
  });

  // Print final JSON summary to stdout
  process.stdout.write(
    JSON.stringify(
      { tick, txHash, simulationResults, resultSemantics: 'simulation-only', clan: CLAN_ID, mission: 'ChopWood-Forest' },
      null,
      2,
    ) + '\n',
  );
}

main().catch(err => {
  console.error('[orchestrator] fatal:', err);
  process.exit(1);
});
