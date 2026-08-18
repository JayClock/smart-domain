---
description: Plan a no-service Smart Domain backend from scenarios, roots, associations, context roles, lifecycle adapters, and HATEOAS projection
argument-hint: "<functional request>"
---
Plan this request using Smart Domain's normative contract in `docs/pattern-contract.md`.

## Request

$ARGUMENTS

## Required flow

1. Call `smart_domain_subagent` with `agent: "smart-domain-architect"` first.
2. Do not propose production code until the plan contains:
   - acceptance scenarios and concrete data;
   - root associations;
   - an association matrix with owner, field, target, cardinality, narrow API, wide operations,
     lifecycle, adapter, and API rel;
   - a context-role matrix when actors vary by context;
   - invariant ownership;
   - a layer matrix for Domain, Persistent, HTTP interface, Agent-tree, and Tests.
3. Reject any design that requires an application service, use-case handler, mediator, facade, or
   repository orchestration layer to own business behavior.
4. Ask for clarification or state explicit assumptions when roots, ownership, cardinality, roles, or
   invariants are ambiguous.
5. Call `smart-domain-test` to validate scenario and architecture coverage for non-trivial work.
6. Call only affected specialists:
   - model contracts and behavior: `smart-domain-domain`;
   - lifecycle adapters and mapping: `smart-domain-persistence`;
   - Jersey/HATEOAS projection: `smart-domain-api`;
   - rel/model-tree discoverability: `smart-domain-agent-tree`;
   - verification: `smart-domain-test`.
7. When contracts are unclear, use chain order:

   ```text
   architect -> test preflight -> domain -> persistence -> API -> agent-tree -> test
   ```

8. Parallelize only after the association and role matrices are stable.

## Final plan

Report scenarios, data, roots, association matrix, role matrix, invariants, per-layer target methods,
test doubles, ordered files/tasks, exact verification commands, and done criteria.
