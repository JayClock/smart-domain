Implement this Smart Domain request using a scenario-first project-subagent workflow:

{{request}}

Required workflow:
1. Call `smart_domain_subagent` with `agent: "smart-domain-architect"` first.
2. Do not edit code until the plan contains:
   - acceptance scenarios;
   - concrete test data;
   - affected-layer matrix for HTTP interface, Application Logic, Persistent, Agent-tree, and Tests.
3. If scenarios or test data are missing, ask for clarification or state explicit assumptions before implementation.
4. Use affected specialist agents only:
   - Application Logic/domain contracts: `smart-domain-domain`.
   - Persistent storage, adapters, hydration, MyBatis, or memory implementations: `smart-domain-persistence`.
   - HTTP interface resources, representations, links, templates, status codes, or media types: `smart-domain-api`.
   - `/agent-tree`, rel navigation, HAL-FORMS discoverability, or demo agent scripts: `smart-domain-agent-tree`.
   - Test design and verification: `smart-domain-test`.
5. If all layers are involved and contracts are unclear, prefer chain mode:
   - architect -> test preflight -> domain -> persistence -> API -> agent-tree -> test.
6. If contracts are clear and layer work is independent, use parallel specialist calls for the affected layers.
7. Make final edits only in the parent agent after reviewing subagent output.
8. Run the narrow checks recommended by `smart-domain-test`.
9. Report:
   - changed files;
   - implemented scenarios;
   - test data used;
   - verification commands and results;
   - any skipped checks or remaining risks.
