---
description: Create a scenario-first Smart Domain plan using this repository's architecture test process
argument-hint: "<functional request>"
---
Use the Smart Domain project subagents to create a scenario-first, test-process-driven plan before editing.

## Current repository stack

This repository is a Java 17 Gradle multi-module Smart Domain product line:

- Build/test: Gradle multi-project, JUnit Platform, Spring Boot dependency management 3.5.9, google-java-format via Spotless.
- HTTP interface: Jersey/JAX-RS resources under `/api`, Spring Boot Jersey `ResourceConfig`, Smart Domain API Jersey auto-configuration.
- API projection: Spring HATEOAS `RepresentationModel`, HAL, HAL-FORMS, affordances/templates, `@VendorMediaType`, vendor media type interceptors.
- Application Logic/domain: POJO-oriented domain model, description objects, association objects (`HasOne`, `HasMany`, `Ref`), context roles, facades/services where present.
- Persistent: in-memory demo adapters, MyBatis adapters, `@EnableSmartDomainMybatis`, mapper/hydration boundaries, Caffeine-backed persistence cache support.
- Agent-tree: JavaParser-based `api-model-tree-tool` that inspects Java HATEOAS API model/resource classes; `/api/accounting/agent-tree`; rel-based navigation; demo agent scripts.
- Verification: module-scoped Gradle tests, Spring Boot tests, `TestRestTemplate`, H2 or existing in-memory fakes where persistence behavior is exercised.

## Architecture test process

For each acceptance scenario, classify only the affected layers.

- If HTTP interface is affected:
  - Use the corresponding Application Logic service/facade/domain port stub as the test double where the codebase has one.
  - List HTTP interface target functions.
  - List HTTP interface target scenarios.
  - Cover status codes, request/response bodies, media types, `_links`, HAL-FORMS `_templates`, affordances, and error mapping when relevant.

- If Application Logic/domain is affected:
  - Use the corresponding Persistent DAO/adapter/repository stub as the test double where the codebase has one.
  - List Application Logic/domain target functions.
  - List Application Logic/domain target scenarios.
  - Cover entity behavior, descriptions, association contracts, context roles, and domain invariants.

- If Persistent is affected:
  - Use H2 or the existing in-memory/fake implementation for verification.
  - If Flyway or schema migration is present for the target module, initialize schema before tests and clean after tests.
  - List Persistent target functions.
  - List Persistent target scenarios.
  - Cover mapping, hydration, lifecycle style, empty results, and cache boundaries.

- If Agent-tree/navigation is affected:
  - Do not hardcode endpoint paths as the agent contract.
  - Verify rels, `_links`, `_templates`, affordances, Java API model tree nodes, and `/agent-tree` discoverability.

- If the request is only about `.pi` prompts/agents/extensions/docs:
  - Classify it as tooling/prompt work.
  - Do not plan production Java edits.
  - Recommend static prompt checks and only run Gradle tests if runtime behavior claims changed.

## Functional request

$ARGUMENTS

## Required flow

1. Call `smart_domain_subagent` with `agent: "smart-domain-architect"` first.
2. Ensure the architect output includes:
   - every acceptance scenario;
   - concrete test data for each scenario;
   - affected-layer matrix for HTTP interface, Application Logic/domain, Persistent, Agent-tree, and Tests;
   - target functions and target scenarios per affected layer;
   - test double/fake strategy per affected layer.
3. If scenarios or test data are missing or ambiguous, ask a clarification or derive explicit assumptions before specialist planning.
4. Call `smart-domain-test` to validate scenario coverage and testing strategy when the change is non-trivial.
5. Call only the affected specialist agents:
   - HTTP interface changes: `smart-domain-api`.
   - Application Logic/domain changes: `smart-domain-domain`.
   - Persistent changes: `smart-domain-persistence`.
   - HATEOAS navigation, rel plans, Java API model tree, or `/agent-tree` changes: `smart-domain-agent-tree`.
   - Verification: `smart-domain-test`.
6. If Application Logic/domain contracts are unclear, analyze them before persistence or API work.
7. Use parallel specialist calls only when contracts are already clear; otherwise use chain mode.
8. Summarize the final plan with:
   - acceptance scenarios and test data;
   - target functions, target scenarios, and test double/fake strategy by layer;
   - tasks grouped by HTTP interface, Application Logic/domain, Persistent, Agent-tree, and Tests;
   - impacted files;
   - exact tests or static checks to run.
