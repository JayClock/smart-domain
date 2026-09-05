# Context Roles Design Template

English | [简体中文](./context-roles.zh-CN.md)

Fill this in the consuming repository alongside the [association matrix](./association-matrix.md).
Replace template links after copying. Read the framework's [Context Roles](../docs/context-roles.md)
and [Pattern Contract](../docs/pattern-contract.md); do not use a role name as an authorization DTO.

## Applicability And Trusted Actor

- Scenario and review owner: `{{SCENARIO_AND_OWNER}}`
- Context-specific capabilities exist / not applicable with reason: `{{APPLICABILITY}}`
- How authentication establishes the actor, and which infrastructure owns it: `{{TRUSTED_ACTOR_SOURCE}}`
- Context lookup, isolation boundary and membership/assignment source: `{{CONTEXT_AND_ASSIGNMENTS}}`

Do not trust a request's role or user ID as authority. A resolver decides whether the authenticated
actor can assume a role in the actual context. Missing membership, forbidden capability, revocation
and absent context need explicit outcomes and a consistent HTTP privacy policy when exposed over HTTP.

## Actor → Context → Role

| ID | Actor | Context | Switcher / resolver | ContextRole type | Capability methods | Domain behaviors | Admission / denial |
| --- | --- | --- | --- | --- | --- | --- | --- |
| ROLE-001 | `{{ACTOR}}` | `{{CONTEXT}}` | `{{SWITCHER_AND_RESOLVER}}` | `{{ROLE}}` | `{{CAPABILITIES}}` | `{{BEHAVIOR_IDS}}` | `{{ADMISSION_AND_DENIAL}}` |

Use `ContextSwitcher<Actor, Context, Role>` and `ContextRole<Actor, Context>` where applicable.
Record the resolver's domain decision separately from the assignment-loading adapter and bootstrap
wiring. A role method delegates to or coordinates connected entities; it does not assemble disconnected
repositories or move domain policy into a security filter.

## Role Acceptance And Risk Cases

| Case | Concrete actor / context / assignments | Invoked capability | Expected domain outcome / unchanged state | Test / evidence |
| --- | --- | --- | --- | --- |
| Allowed | `{{ALLOWED_DATA}}` | `{{METHOD}}` | `{{SUCCESS}}` | `{{TEST}}` |
| Denied | `{{DENIED_DATA}}` | `{{METHOD}}` | `{{DENIAL}}` | `{{TEST}}` |
| Cross-context access | `{{OTHER_CONTEXT_DATA}}` | `{{METHOD}}` | `{{ISOLATION}}` | `{{TEST}}` |
| Revoked or changed assignment | `{{REVOCATION_DATA}}` | `{{METHOD}}` | `{{REVOCATION_POLICY}}` | `{{TEST}}` |

State how role resolution and mutation interact under concurrency, whether assignments are rechecked,
and which audit events exclude secrets: `{{CONCURRENCY_AND_AUDIT}}`.
Mark unneeded cases not applicable with a reason. A domain role test cannot prove the real identity
provider or token verification; link selected integration/security checks in the [adoption checklist](./adoption-checklist.md).
