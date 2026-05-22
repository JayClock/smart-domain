---
description: Implement a Smart Domain request using scenario-first TDD and project subagents
argument-hint: "<functional request>"
---
Implement this Smart Domain request using this repository's scenario-first architecture test process and TDD-style execution.

## Current repository stack

This repository is a Java 17 Gradle multi-module Smart Domain product line:

- Build/test: Gradle multi-project, JUnit Platform, Spring Boot dependency management 3.5.9, Spotless/google-java-format.
- HTTP interface: Jersey/JAX-RS resources under `/api`, Spring Boot Jersey configuration, Smart Domain API Jersey auto-configuration.
- API projection: Spring HATEOAS `RepresentationModel`, HAL, HAL-FORMS, affordances/templates, `@VendorMediaType`, vendor media type interceptors.
- Application Logic/domain: POJO-oriented domain model, description objects, association objects (`HasOne`, `HasMany`, `Ref`), context roles, facades/services where present.
- Persistent: in-memory demo adapters, MyBatis adapters, `@EnableSmartDomainMybatis`, mapper/hydration boundaries, Caffeine-backed persistence cache support.
- Agent-tree: JavaParser-based `api-model-tree-tool` that inspects Java HATEOAS API model/resource classes; `/api/accounting/agent-tree`; rel-based navigation; demo agent scripts.
- Verification: module-scoped Gradle tests, Spring Boot tests, `TestRestTemplate`, H2 or existing in-memory fakes where persistence behavior is exercised.

## Functional request

$ARGUMENTS

## Required workflow

1. Call `smart_domain_subagent` with `agent: "smart-domain-architect"` first.
2. Do not edit code until the plan contains:
   - acceptance scenarios;
   - concrete test data;
   - affected-layer matrix for HTTP interface, Application Logic/domain, Persistent, Agent-tree, and Tests;
   - target functions and target scenarios for every affected layer;
   - test double/fake strategy for every affected layer.
3. If scenarios or test data are missing, ask for clarification or state explicit assumptions before implementation.
4. Use affected specialist agents only:
   - Application Logic/domain contracts: `smart-domain-domain`.
   - Persistent storage, adapters, hydration, MyBatis, Caffeine/cache, memory implementations: `smart-domain-persistence`.
   - HTTP resources, Spring HATEOAS representations, HAL links, HAL-FORMS templates, affordances, status codes, or media types: `smart-domain-api`.
   - `/agent-tree`, rel navigation, Java API model tree discoverability, or demo agent scripts: `smart-domain-agent-tree`.
   - Test design and verification: `smart-domain-test`.
5. If all layers are involved and contracts are unclear, prefer chain mode:
   - architect -> test preflight -> domain -> persistence -> API -> agent-tree -> test.
6. If contracts are clear and layer work is independent, use parallel specialist calls for the affected layers.
7. Make final edits only in the parent agent after reviewing subagent output.

## TDD execution rule

For production behavior changes, proceed scenario by scenario:

1. Write or update the narrowest test for the next acceptance scenario.
2. Run the narrowest Gradle test command and confirm RED, unless the existing test already fails for the expected reason.
3. Implement the smallest production change that makes that test pass.
4. Re-run the narrowest command and confirm GREEN.
5. Refactor only after GREEN; then re-run the affected tests.
6. Do not implement future scenarios before their tests exist.

Layer-specific testing guidance:

- HTTP interface: test Jersey/JAX-RS resource behavior with Application Logic stubs where available; assert status, body, media type, `_links`, `_templates`, and affordances.
- Application Logic/domain: test domain/use-case behavior with Persistent stubs where available; assert associations, context roles, descriptions, and invariants.
- Persistent: test against H2 or existing in-memory fake; use Flyway/schema migration when present; assert mapping, hydration, lifecycle style, empty results, and cache boundaries.
- Agent-tree: test rel navigation and discoverability through `_links`, `_templates`, Java API model tree output, and `/agent-tree`, not hardcoded URLs.

If the request is only about `.pi` prompts/agents/extensions/docs:

- Treat it as tooling/prompt work.
- Do not change production Java code.
- Prefer static checks for prompt metadata, agent references, stack anchors, and stale framework wording.
- Run Gradle tests only if the prompt change makes or changes claims about runtime behavior.

## Final report

Report:

- changed files;
- implemented scenarios;
- test data used;
- RED/GREEN/refactor steps for production changes, or static prompt checks for `.pi`-only changes;
- verification commands and results;
- skipped checks or remaining risks.
