# Project Pi Subagents

This project provides a Pi extension for Smart Domain layered work.

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

- `smart-domain-architect` — scenario-first coordinator; lists acceptance scenarios, test data, layer impact, and specialist flow.
- `smart-domain-domain` — Application Logic/domain specialist for entities, descriptions, associations, context roles, and use-case/service/facade contracts.
- `smart-domain-persistence` — Persistent specialist for memory adapters, MyBatis adapters, DAOs/mappers where present, hydration, and lifecycle boundaries.
- `smart-domain-api` — HTTP interface/HATEOAS specialist for Jersey resources, API models, links, templates, status codes, and media types.
- `smart-domain-agent-tree` — agent-navigation specialist for `/agent-tree`, rel paths, HAL-FORMS discoverability, and demo scripts.
- `smart-domain-test` — scenario and verification specialist for test data, narrow Gradle commands, assertions, and boundary risks.

## Scenario-first workflow

Planning and implementation prompts should follow this order:

1. Call `smart-domain-architect`.
2. List every acceptance scenario and concrete test data.
3. Build a layer impact matrix:
   - HTTP interface
   - Application Logic
   - Persistent
   - Agent-tree
   - Tests
4. Call only affected specialist agents.
5. Keep Application Logic/domain contracts ahead of persistence or HTTP design when contracts are unclear.
6. Use `smart-domain-test` to map tests and assertions back to scenario ids.

Layer testing guidance:

- HTTP interface: use the corresponding Application Logic service/facade/domain port stub where the codebase has one.
- Application Logic: use the corresponding Persistent DAO/adapter/repository stub where the codebase has one.
- Persistent: prefer H2 or existing fake/in-memory implementations for verification.

## Prompt templates

- `/smart-domain-plan request=...`
- `/smart-domain-implement request=...`

Both templates enforce scenario-first planning before editing.

## Example tool call shape

```json
{
  "chain": [
    { "agent": "smart-domain-architect", "task": "Create a scenario-first plan for: ..." },
    { "agent": "smart-domain-test", "task": "Using prior result: {previous}\nValidate scenarios, test data, and verification strategy." },
    { "agent": "smart-domain-domain", "task": "Using prior result: {previous}\nAnalyze Application Logic/domain impact." },
    { "agent": "smart-domain-persistence", "task": "Using prior result: {previous}\nAnalyze Persistent impact." },
    { "agent": "smart-domain-api", "task": "Using prior result: {previous}\nAnalyze HTTP interface impact." },
    { "agent": "smart-domain-agent-tree", "task": "Using prior result: {previous}\nAnalyze agent-tree discoverability impact." },
    { "agent": "smart-domain-test", "task": "Using prior result: {previous}\nRecommend final tests." }
  ]
}
```
