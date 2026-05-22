Use the Smart Domain project subagents to create a scenario-first implementation plan before editing:

{{request}}

Required flow:
1. Call `smart_domain_subagent` with `agent: "smart-domain-architect"` first.
2. Ensure the architect output includes:
   - every acceptance scenario;
   - concrete test data for each scenario;
   - a layer impact matrix for HTTP interface, Application Logic, Persistent, Agent-tree, and Tests.
3. If scenarios or test data are missing or ambiguous, ask a clarification or derive explicit assumptions before specialist planning.
4. Call `smart-domain-test` to validate scenario coverage and testing strategy when the change is non-trivial.
5. Call only the affected specialist agents:
   - HTTP interface changes: `smart-domain-api`.
   - Application Logic/domain changes: `smart-domain-domain`.
   - Persistent changes: `smart-domain-persistence`.
   - HATEOAS navigation or `/agent-tree` changes: `smart-domain-agent-tree`.
   - Verification: `smart-domain-test`.
6. If Application Logic/domain contracts are unclear, analyze them before persistence or API work.
7. Use parallel specialist calls only when contracts are already clear; otherwise use chain mode.
8. Summarize the final plan with:
   - acceptance scenarios and test data;
   - tasks grouped by HTTP interface, Application Logic, Persistent, Agent-tree, and Tests;
   - impacted files;
   - exact tests or checks to run.
