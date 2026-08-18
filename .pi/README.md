# Project Pi Subagents

This project provides Pi agents and prompts for maintaining and extending Smart Domain with its
association-object, no-service architecture.

## Canonical contract

All agents must read and obey:

1. `docs/pattern-contract.md`
2. `docs/association-recipes.md`
3. `docs/anti-patterns.md`
4. `AGENTS.md`

The canonical call path is:

```text
HTTP resource -> root association -> entity/context role -> association -> adapter
```

Agents must not generate an application service, use-case handler, mediator, or facade that owns
business behavior.

## Repository stack

- Java 17 Gradle multi-module product line with JUnit Platform.
- POJO domain entities, descriptions, root associations, `HasOne`/`HasMany`/`Ref`, and context
  roles.
- Memory and MyBatis association adapters with progressive lifecycle choices.
- Jersey/JAX-RS plus Spring HATEOAS, HAL, HAL-FORMS, affordances, and vendor media types.
- JavaParser-based `api-model-tree-tool` and `/api/accounting/agent-tree` for rel-driven navigation.

## Extension

The auto-discovered extension at `.pi/extensions/smart-domain-subagents/index.ts` registers:

- tool: `smart_domain_subagent`
- command: `/smart-agents`

Reload Pi after pulling changes:

```text
/reload
```

## Agents

- `smart-domain-architect` — acceptance scenarios, association graph, role matrix, layer impact, and
  ordered work.
- `smart-domain-domain` — root associations, entities, descriptions, narrow/wide associations,
  context roles, and behavior.
- `smart-domain-persistence` — memory/MyBatis adapters, hydration, lifecycle, and cache boundaries.
- `smart-domain-api` — direct graph projection through Jersey/HATEOAS/HAL-FORMS.
- `smart-domain-agent-tree` — rel paths, model-tree discovery, and agent navigation.
- `smart-domain-test` — scenario coverage, association contracts, architecture boundaries, and
  verification.

## Required planning sequence

Before production edits, the architect must provide:

1. acceptance scenarios and concrete test data;
2. root associations;
3. an association matrix with owner, field, target, cardinality, narrow API, wide operations,
   lifecycle, adapter, and API rel;
4. a context-role matrix when actors vary by context;
5. invariants and observable outcomes;
6. a layer impact matrix for Domain, Persistent, HTTP interface, Agent-tree, and Tests.

Then call only affected specialists. Domain contracts come before persistence and HTTP design.
Production behavior follows scenario-by-scenario Red-Green-Refactor.

## Prompt templates

- `/smart-domain-plan <request>`
- `/smart-domain-implement <request>`

Both use `$ARGUMENTS`, invoke `smart-domain-architect` first, and reject service-first designs.

## Portable skill

Coding agents that support repository-local skills can use or copy the complete directory:

```text
.agents/skills/smart-domain-backend/
```

The directory bundles its pattern references and eval set. It checks whether generated backends
preserve root navigation, narrow/wide association interfaces, entity/context-role behavior,
lifecycle adapters, and direct HATEOAS projection.
