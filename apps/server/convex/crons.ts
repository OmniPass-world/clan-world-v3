import { cronJobs } from "convex/server";
import { internal } from "./_generated/api";

const crons = cronJobs();

if (process.env.CLANWORLD_USE_FAKE_HEARTBEAT === "true") {
  crons.interval("heartbeat-safety-net", { seconds: 5 }, internal.heartbeat.advanceTick, {});
}

export default crons;
