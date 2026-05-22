# Project Pi Subagents

This project provides a Pi extension and prompt set for Smart Domain layered work.

## Current repository stack encoded in prompts

The project-local prompts and agents should describe this repository as:

- Java 17 Gradle multi-module Smart Domain product line.
- Spring Boot dependency management 3.5.9 and JUnit Platform tests.
- Jersey/JAX-RS HTTP resources with Spring Boot Jersey configuration.
- Spring HATEOAS `RepresentationModel`, HAL, HAL-FORMS, affordances/templates, `_links`, `_templates`, and `@VendorMediaType`.
- Application Logic/domain model with POJO entities, description objects, association objects (`HasOne`, `HasMany`, `Ref`), and context roles.
- Persistent layer with in-memory demo adapters, MyBatis adapters, `@EnableSmartDomainMybatis`, hydration/lifecycle boundaries, and Caffeine-backed cache support.
- JavaParser-based `api-model-tree-tool` for Java HATEOAS API model/resource classes, plus runtime `/api/accounting/agent-tree` and demo agent scripts.

## Extension

Auto-discovered extension:

```text
.pi/extensions/smart-domain-subagents/index.ts
```

It registers:

- tool: `smart_domain_subagent`
- command: `/smart-agents`

Reload Pi after pulling these files:

```text
/reload
```

## Agents

Project-local agent definitions live in `.pi/agents/`:

- `smart-domain-architect` — scenario-first coordinator; lists acceptance scenarios, test data, layer impact, target functions/scenarios, test-double strategy, and specialist flow.
- `smart-domain-domain` — Application Logic/domain specialist for Java POJO entities, descriptions, associations, context roles, and use-case/service/facade contracts.
- `smart-domain-persistence` — Persistent specialist for memory adapters, MyBatis adapters, `@EnableSmartDomainMybatis`, hydration, lifecycle boundaries, and Caffeine/cache behavior.
- `smart-domain-api` — HTTP interface/HATEOAS specialist for Jersey/JAX-RS resources, Spring HATEOAS models, HAL links, HAL-FORMS templates, affordances, status codes, and media types.
- `smart-domain-agent-tree` — agent-navigation specialist for Java API model tree discovery, `/agent-tree`, rel paths, HAL-FORMS discoverability, and demo scripts.
- `smart-domain-test` — scenario and verification specialist for TDD order, test data, narrow Gradle commands, assertions, static prompt checks, and boundary risks.

## Scenario-first architecture test workflow

Planning and implementation prompts should follow this order:

1. Call `smart-domain-architect`.
2. List every acceptance scenario and concrete test data.
3. Build a layer impact matrix:
   - HTTP interface
   - Application Logic/domain
   - Persistent
   - Agent-tree
   - Tests
4. For every affected layer, list:
   - target functions
   - target scenarios
   - test double or fake strategy
5. Call only affected specialist agents.
6. Keep Application Logic/domain contracts ahead of persistence or HTTP design when contracts are unclear.
7. Use `smart-domain-test` to map tests and assertions back to scenario ids.
8. For production changes, execute Red-Green-Refactor scenario by scenario.

Layer testing guidance:

- HTTP interface: use the corresponding Application Logic service/facade/domain port stub where the codebase has one; assert status, body, media type, `_links`, `_templates`, and affordances.
- Application Logic/domain: use the corresponding Persistent DAO/adapter/repository stub where the codebase has one; assert domain behavior, associations, context roles, and invariants.
- Persistent: prefer H2 or existing fake/in-memory implementations; use schema migration such as Flyway only when present for the target module; assert mapping, hydration, lifecycle style, empty results, and cache boundaries.
- Agent-tree: verify rels, Java API model tree nodes, `_links`, `_templates`, and `/agent-tree` discoverability instead of hardcoded endpoint paths.
- `.pi` prompt/tooling-only changes: use static checks and avoid production Java edits.

## Prompt templates

- `/smart-domain-plan <request>`
- `/smart-domain-implement <request>`

Both templates use `$ARGUMENTS` expansion and enforce scenario-first planning before editing.

## Example tool call shape

```json
{
  "chain": [
    { "agent": "smart-domain-architect", "task": "Create a scenario-first plan for: ..." },
    { "agent": "smart-domain-test", "task": "Using prior result: {previous}\nValidate scenarios, test data, TDD order, and verification strategy." },
    { "agent": "smart-domain-domain", "task": "Using prior result: {previous}\nAnalyze Application Logic/domain impact." },
    { "agent": "smart-domain-persistence", "task": "Using prior result: {previous}\nAnalyze Persistent impact." },
    { "agent": "smart-domain-api", "task": "Using prior result: {previous}\nAnalyze HTTP interface impact." },
    { "agent": "smart-domain-agent-tree", "task": "Using prior result: {previous}\nAnalyze Java API model tree and agent-tree discoverability impact." },
    { "agent": "smart-domain-test", "task": "Using prior result: {previous}\nRecommend final tests and static checks." }
  ]
}
```
