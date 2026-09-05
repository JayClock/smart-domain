# Association And Behavior Design Template

English | [简体中文](./association-matrix.zh-CN.md)

Copy into the consuming repository and fill it before implementing the selected scenario.
Replace template links for the destination. Follow the [Pattern Contract](../docs/pattern-contract.md)
and pair this record with [context roles](./context-roles.md) and [verification](./adoption-checklist.md).
Rows below are placeholders, not an approved business model.

## Scope And Traceability

- Project / selected scenario / concrete acceptance data: `{{PROJECT_SCENARIO_DATA}}`
- Requirements and context/container diagram: `{{REQUIREMENTS_AND_DIAGRAM_PATHS}}`
- Smart Domain version / documentation revision: `{{VERSION_AND_DOCS_REF}}`
- Decision owner / review status / open questions: `{{REVIEW_AND_QUESTIONS}}`

## Connected Object Graph

`{{ROOTS_OWNERS_NAVIGATION_AND_CONTEXT_BOUNDARIES}}`

Describe at least one root for each graph. Explain `Ref` identity-only facts versus navigation;
use `HasOne` only when a target is guaranteed, and a named `Optional` association for meaningful absence.
Model relationships with their own identity, attributes or lifecycle as entities. Do not turn every
foreign key into navigation or use ID jumps where the model needs an association.

## Association Matrix

Use stable project IDs. Identify read-only associations explicitly; root creation may be a root operation.

| ID | Root | Owner | Field | Target | Cardinality | Public read API | Internal wide operations | Lifecycle | Adapter | API rel |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ASSOC-001 | `{{ROOT}}` | `{{OWNER}}` | `{{FIELD}}` | `{{TARGET}}` | `{{CARDINALITY}}` | `{{READ_API}}` | `{{WIDE_INTERFACE_AND_OPERATIONS}}` | `{{LIFECYCLE}}` | `{{ADAPTER}}` | `{{REL_OR_NA}}` |

Keep `Owner.field → Owner.WideInterface → OwnerField adapter` correspondence. Record why the
lifecycle is root, aggregated, reference, remote or projection; changing it must not change the domain
contract. List each planned fake/production implementation, and distinguish fixtures from durable storage.

## Behavior, Invariants And Atomicity

| ID | Scenario | Root / owning entity or role method | Preconditions / invariant | Association IDs | Failure / unchanged state | Atomicity / concurrent case |
| --- | --- | --- | --- | --- | --- | --- |
| BEH-001 | `{{SCENARIO}}` | `{{DOMAIN_ENTRY}}` | `{{RULE}}` | ASSOC-001 | `{{FAILURE}}` | `{{ATOMICITY}}` |

For each operation specify the transaction boundary, optimistic/locking mechanism, retries/idempotency
and external side-effect failure handling where applicable. Business policy belongs to the owner/role;
adapters enforce the storage and concurrency mechanism. Map every invariant to a test, including races
that a sequential fake cannot establish.

## API Projection And Compatibility

| Operation | Domain navigation / behavior | Method / path / rel / affordance | Input Description or value | Output / pagination | Error / existing contract |
| --- | --- | --- | --- | --- | --- |
| `{{OPERATION}}` | BEH-001 / ASSOC-001 | `{{HTTP_CONTRACT}}` | `{{INPUT}}` | `{{OUTPUT}}` | `{{ERROR_AND_COMPATIBILITY}}` |

Record which public associations are navigable and how allowed operations are exposed. If no HTTP
surface is selected, give the reason rather than inventing endpoints. Do not include server paths,
credentials or internal persistence types in public representations.

## Project Glossary And Non-Functional Constraints

| English term / identifier | Chinese term | Meaning / owner | Unit, format or prohibited synonym |
| --- | --- | --- | --- |
| `{{TERM}}` | `{{CHINESE_TERM}}` | `{{MEANING}}` | `{{CONSTRAINT}}` |

Record applicable authorization, audit, privacy, localization, performance and operability constraints
with measurement, owner and evidence path: `{{QUALITY_CONSTRAINTS}}`. Unknown thresholds remain open
questions; they are not implicitly approved. Link the filled adoption checklist before implementation.
