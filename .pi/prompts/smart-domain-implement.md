---
description: Implement a no-service Smart Domain request association-first with scenario-by-scenario TDD
argument-hint: "<functional request>"
---
Implement this request according to `docs/pattern-contract.md`.

## Request

$ARGUMENTS

## Planning gate

1. Call `smart_domain_subagent` with `agent: "smart-domain-architect"` first.
2. Do not edit production code until the result contains:
   - acceptance scenarios and concrete data;
   - root associations;
   - the complete association matrix;
   - context roles and role methods;
   - invariant ownership;
   - Domain/Persistent/HTTP/Agent-tree/Test impact.
3. Reject service-first designs. Business behavior must enter through a root, entity, or context
   role and reach storage through entity-owned association contracts.
4. Ask for clarification or state assumptions when the graph is ambiguous.

## Specialist flow

Use only affected agents:

- `smart-domain-domain` for roots, entities, descriptions, associations, roles, and behavior;
- `smart-domain-persistence` for memory/MyBatis adapters, hydration, lifecycle, and caches;
- `smart-domain-api` for direct Jersey/HATEOAS graph projection;
- `smart-domain-agent-tree` for rel navigation and model-tree discovery;
- `smart-domain-test` for scenario, contract, architecture, and prompt verification.

Prefer chain order when contracts are unclear:

```text
architect -> test preflight -> domain -> persistence -> API -> agent-tree -> test
```

Make final edits in the parent agent after reviewing specialist output.

## TDD execution

For each production scenario:

1. write the narrowest direct domain, adapter-contract, or HTTP projection test;
2. run it and confirm the expected RED;
3. implement the smallest change;
4. rerun and confirm GREEN;
5. refactor only after GREEN;
6. continue to the next scenario.

Implement in conceptual order: domain graph and behavior, association fakes, production lifecycle
adapters, then HTTP/HATEOAS projection. Do not start from tables or controllers.

## Final report

Report changed files, implemented scenarios, association/role decisions, RED/GREEN evidence,
verification commands/results, and remaining risks.
