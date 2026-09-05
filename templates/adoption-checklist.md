# Adoption Verification Template

English | [简体中文](./adoption-checklist.zh-CN.md)

Copy into the consuming repository, repair template links, and connect the filled
[association matrix](./association-matrix.md) and [context roles](./context-roles.md).
This is an evidence record, not a claim that checks already exist or have passed.

## Baseline And Decisions

- Selected scope, requirements and protected existing contracts: `{{SCOPE_AND_CONTRACTS}}`
- Artifact version / dependency repository / documentation revision: `{{VERSION_SOURCE_DOCS}}`
- JDK, framework, build tool, database and selected integrations: `{{HOST_AND_INTEGRATIONS}}`
- Compatibility ADR, alternatives, consequences and rollback: `{{ADR}}`
- Design records, open decisions and approval owner: `{{DESIGN_AND_OWNER}}`

Complete applicable design fields before implementation. Record not-applicable reasons explicitly;
a missing environment is blocked verification, not an automatic not-applicable decision.

## Design Readiness

- [ ] Goals, concrete acceptance data, terminology, scope and non-goals are agreed.
- [ ] Compatibility is reviewed; stack changes are approved rather than inferred from a demo.
- [ ] Roots, connected navigation and the association matrix are complete.
- [ ] Mutable owner associations have narrow public read interfaces and private wide fields.
- [ ] Each invariant/state transition has an owning entity or context role and a failure outcome.
- [ ] Role capabilities, trusted actor resolution and denial cases are specified where applicable.
- [ ] Module dependencies, API rel/affordance mapping and existing contract compatibility are defined.
- [ ] Atomic operations, retries/idempotency, concurrent invariants and external failures are designed.
- [ ] Exact project commands, prerequisites and risk-based quality thresholds are known.

## Planned Checks And Observed Results

Use one row per executable check or manual procedure. Status is `planned`, `passed`, `failed`,
`blocked` or `not-applicable`. Fill the observed result only after execution; include commit, command,
exit code or review outcome, evidence path and date. Preserve failing evidence when rerunning.

| ID / behavior or association | Purpose / quadrant | Subject and real dependency vs double | Working directory / exact command or procedure | Expected outcome | Status / observed evidence | Owner / NA reason |
| --- | --- | --- | --- | --- | --- | --- |
| CHECK-001 / `{{BEHAVIOR_IDS}}` | Domain decisions / Q1 | Domain + association fake | `{{DOMAIN_CHECK}}` | `{{INVARIANT_AND_FAILURE}}` | planned | `{{OWNER}}` |
| CHECK-002 / `{{ASSOCIATION_IDS}}` | Shared adapter contract / Q1 | Each selected adapter; identify real storage | `{{ADAPTER_CHECK}}` | `{{SAME_OBSERVABLE_CONTRACT}}` | planned | `{{OWNER}}` |
| CHECK-003 / `{{SCENARIO_IDS}}` | Business acceptance / Q2 | Selected HTTP/runtime and dependencies | `{{ACCEPTANCE_CHECK}}` | `{{SCENARIO_AND_COMPATIBILITY}}` | planned | `{{OWNER}}` |
| CHECK-004 | Architecture / build / Q1 | Consumer source and build | `{{QUALITY_CHECK}}` | `{{DEPENDENCIES_AND_BUILD}}` | planned | `{{OWNER}}` |
| CHECK-005 | Product evaluation / Q3 | Actual user-facing flow | `{{USABILITY_PROCEDURE}}` | `{{CRITERIA}}` | planned | `{{OWNER}}` |
| CHECK-006 | Security / reliability / performance / Q4 | Real risk boundary | `{{RISK_CHECK}}` | `{{THRESHOLDS}}` | planned | `{{OWNER}}` |

The quadrants describe purpose, not fixed test frameworks. Select applicable cases and add rows rather
than treating this table as a mandatory choice of database, browser tool or performance threshold.

## Evidence Coverage

- [ ] Domain rules run without HTTP/database; fakes do not reimplement the behavior under test.
- [ ] Each production adapter runs the same observable association contract, not a weaker copy.
- [ ] Selected real database tests cover hydration, pagination, rollback and concurrent invariants.
- [ ] Selected HTTP tests cover roots, links, operations, media types, errors and old endpoints.
- [ ] Selected authentication/security checks cover real token/identity handling and isolation.
- [ ] Dependency/API-surface checks enforce layer direction and guard against public mutable associations.
- [ ] A human reviewed business ownership; scans alone do not prove no-service compliance.
- [ ] Required test/format/build checks were run in the consumer, with actual results recorded.

Smart Domain's `./gradlew smartDomainCheck` only checks its accounting demo. It is not a checker
installed in the consumer by a Maven dependency. A green upstream build or consumer sample is not
substitute evidence for the consuming application.

## Review And Exit

| Remaining risk / deviation | Impact | Mitigation / follow-up | Reviewer / decision / date |
| --- | --- | --- | --- |
| `{{RISK_OR_NONE}}` | `{{IMPACT}}` | `{{ACTION}}` | `{{REVIEW}}` |

- Required checks all passed, or remaining blockers are explicitly reported: `{{RESULT_SUMMARY}}`
- Behavior ownership and full-pattern vs partial-integration conclusion: `{{COMPLIANCE_REVIEW}}`
- Project approval reference and follow-up owner: `{{APPROVAL}}`

Do not claim completion while required checks are blocked or failed. Accepted deviations must remain
visible; installing the packages or filling the tables is not proof of full pattern compliance.
