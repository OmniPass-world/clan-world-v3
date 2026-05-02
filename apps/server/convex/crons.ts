import { cronJobs } from "convex/server";
import { internal } from "./_generated/api";

const crons = cronJobs();

if (process.env.CLANWORLD_USE_FAKE_HEARTBEAT === "true") {
  crons.interval("heartbeat-safety-net", { seconds: 5 }, internal.heartbeat.advanceTick, {});
}

if (process.env.CLANWORLD_USE_REAL_INDEXER === "true") {
  crons.interval("real-indexer-snapshot-refresh", { seconds: 5 }, internal.indexer.refreshSnapshot, {});
  crons.interval("real-indexer-log-poller", { seconds: 3 }, internal.indexer.pollLogs, {});
}

export default crons;
