not good ADR format but want to capture plannimg conversation decisions


Afew choices the contract dev should know about:
	1.	I made BanditState.NONE = 0 so bandit.state == NONE is the natural “no active bandit” check. v4.2 omitted this; adding it avoids the ambiguous default value.
	2.	I gave granular event payloads (e.g. BanditAttackResolved includes wallLevelAfter and stolen amounts inline). The indexer can render attack drama without secondary calls.
	3.	ScheduledMarketActionCommitted is a new event — fires when a market mission’s scheduled action is queued. The UI uses this to show “trade pending” indicators before execution.
Flag any disagreements and I’ll revise.

