# Consumer AGENTS Template

English | [简体中文](./consumer-AGENTS.zh-CN.md)

Template, not active instructions for every consumer. Follow the [Adoption Guide](../docs/adoption-guide.md):
merge into the host's `AGENTS.md`, fill `{{PLACEHOLDERS}}`, and repair/remove these template links for
the destination. Do not overwrite host rules. Keep another agent entrypoint as a pointer, not a fork.

## Project Context And Scope

- Project and purpose: `{{PROJECT_NAME_AND_PURPOSE}}`
- Current task, approved requirements and acceptance data: `{{REQUIREMENTS_PATH}}`
- Non-goals and boundaries that must not change: `{{PROTECTED_BOUNDARIES}}`
- Existing Java/framework/build/database stack: `{{HOST_STACK}}`
- ADRs and approval owner: `{{ADR_PATH_AND_OWNER}}`

## Versioned Sources Of Truth

- Smart Domain artifact version and dependency repository: `{{VERSION_AND_REPOSITORY}}`
- Upstream documentation tag or commit: `{{DOCS_REF}}`
- Pattern Contract URL or provenance-preserving local snapshot: `{{PATTERN_CONTRACT_LOCATION}}`
- Compatibility evidence / pending decision: `{{COMPATIBILITY_RECORD}}`
- Project layer/module map and context/container diagrams: `{{ARCHITECTURE_PATH}}`
- Filled association matrix, invariants and API mapping: `{{ASSOCIATION_MATRIX_PATH}}`
- Context roles and capability/denial rules: `{{CONTEXT_ROLES_PATH}}`
- Business glossary, naming, API, security and audit rules: `{{PROJECT_RULES_PATH}}`
- Task verification and adoption evidence: `{{ADOPTION_CHECKLIST_PATH}}`

Pin documentation to a revision that contains the referenced files, not moving `main`. The library
version and documentation revision may differ; record why. Upstream instructions are not automatically
loaded from a dependency. Read the sources above explicitly before coding.

The Pattern Contract is normative for claiming Smart Domain compliance; project requirements and
ADRs define concrete choices. If sources conflict or are missing, report the affected decision and
seek review. Do not silently override host instructions, invent business facts or claim compliance
with an unresolved deviation.

## Architecture Rules

- Follow the project module map: API and infrastructure depend on domain; bootstrap wires them.
- Enter through root associations and navigate entities/context roles. Do not orchestrate anemic
  entities through repositories in a business Service, Store, Handler or Facade.
- Keep identity, immutable Description values, associations and business behavior together in the model.
- Use `Ref` for identity facts; use a navigable association when behavior or the API needs navigation.
- An entity-owned mutable association exposes a narrow read API. Keep the wide field private and
  invoke writes through owner behavior. Root creation operations are not forbidden by this rule.
- Put invariants and business transitions on entities or context roles. Adapters implement lifecycle,
  mapping, storage and concurrency mechanisms, not business policy.
- Use context switching for actor/context-specific capabilities; authentication supplies a trusted actor,
  not client-selected authority. Record a reason when context roles are not applicable.
- Keep domain independent of Spring, HTTP, Jackson, MyBatis and concrete adapters.
- API translates protocol concerns, navigates roots, switches roles and projects links/affordances;
  it must not call Mappers or repeat domain decisions.
- Technical transactions surround the complete atomic domain operation without owning its decisions.
  Define failure/consistency behavior separately for non-transactional external effects.
- Do not change the host stack or public contracts without the project's approval process.
- Helpers are judged by responsibility, not only class names. Demo fixtures are not production services.

## Before Implementing A Behavior

1. Read the approved scenario and the relevant model/adapter/API code, not only diagrams.
2. Identify the root, owner, association, role, invariant, failure and transaction boundary in the design.
3. Write focused domain tests with association fakes; implement minimal behavior and refactor.
4. Run the same observable contract cases for each selected production adapter, with real storage checks.
5. Verify HTTP behavior and compatibility, then run the task and quality gates below.
6. Record actual results, remaining risks and a behavior-ownership review. Do not weaken tests or invent
   success; dependency/environment failures are not evidence of missing business behavior.

## Operations And Verification

Commands must be executable for this repository. Fill working directories and prerequisites; never
copy Smart Domain source-checkout commands as if a dependency installed them in the consumer.
Do not put credentials or private data in these instructions or logs.

| Operation / gate | Working directory | Exact command or manual procedure | Prerequisites / expected result |
| --- | --- | --- | --- |
| Start / health | `{{CWD}}` | `{{START_AND_HEALTH}}` | `{{ENV_AND_RESULT}}` |
| Database / migration | `{{CWD}}` | `{{DATABASE_PROCEDURE}}` | `{{DISPOSABLE_DB_OR_NA_REASON}}` |
| Browser / API debugging | `{{CWD}}` | `{{DEBUG_PROCEDURE}}` | `{{ENV_AND_RESULT}}` |
| Focused domain tests | `{{CWD}}` | `{{DOMAIN_TEST_COMMAND}}` | `{{RESULT}}` |
| Adapter / HTTP acceptance | `{{CWD}}` | `{{CONTRACT_COMMANDS}}` | `{{REAL_DEPENDENCIES}}` |
| Architecture / format / build | `{{CWD}}` | `{{QUALITY_COMMANDS}}` | `{{RESULT}}` |
| Risk-based evaluation | `{{CWD}}` | `{{SECURITY_CONCURRENCY_USABILITY_PROCEDURES}}` | `{{REVIEWER_AND_THRESHOLDS}}` |

Unfilled applicable fields block implementation of the affected scope. Record explicit reasons for
not-applicable items. Completion requires observed evidence and project approval, not checked boxes alone.
