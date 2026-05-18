// This file re-exports the canonical Convex schema from @clan-world/sdk/convex/schema.
// `convex dev` hot-reload only watches files under apps/server/convex/. When you edit
// the SDK schema, touch this file (or run `pnpm gen:convex` in apps/server) to retrigger
// the Convex CLI codegen pass.
export {
  default,
  goldQuoteFields,
  goldQuoteInputFields,
  humanSteeringMessageFields,
  humanSteeringMessageInputFields,
  kickstartTokenFields,
  kickstartTokenInputFields,
  orchEventFields,
  orchEventInputFields,
  whisperFields,
  whisperInputFields,
} from "@clan-world/sdk/convex/schema";
