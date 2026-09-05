# Consumer Adoption Guide

English | [简体中文](./adoption-guide.zh-CN.md)

**Organize code in layers; execute business behavior through the connected domain graph.**
This guide turns the normative [Pattern Contract](./pattern-contract.md) into a consumer workflow.
It does not introduce a Controller → Service → Repository architecture or require every module.

## 1. Separate Framework Rules From Project Decisions

| Feed-forward context | Smart Domain supplies | The consuming repository supplies |
| --- | --- | --- |
| Why: project context | Fit, constraints, pattern contract and reading order | Business goals, existing stack, scope, non-goals and protected boundaries |
| What: requirements | Design and acceptance templates | Concrete scenarios, domain facts and unresolved questions |
| How: architecture | Layer responsibilities, association recipes and runnable examples | Context/container diagrams, object graph, module paths, lifecycle and transaction decisions |
| Knowledge and operations | Framework terminology, compatibility and integration guides | Business glossary, startup/database/debugging instructions and environment prerequisites |
| Rules and evidence | Anti-patterns and adoption checklist | API/security/audit/localization requirements, exact checks, results and reviewers |

Do not copy accounting business rules into a consumer merely because they appear in the demo.
Keep project requirements and design decisions in the consuming repository, not in this library.
If it has an artifact approval workflow, use that workflow rather than creating competing documents.

## 2. Establish An Explicit AI Entry Point

1. Select the language of the [consumer AGENTS template](../templates/consumer-AGENTS.md).
2. Merge it into the consuming repository's `AGENTS.md`; do not overwrite existing instructions.
3. Fill all placeholders and replace template/counterpart links with valid destination links.
4. Point it to the project's filled [association matrix](../templates/association-matrix.md),
   [context roles](../templates/context-roles.md) and [adoption checklist](../templates/adoption-checklist.md).
5. If another agent needs `CLAUDE.md`, make it a short pointer to the same instructions and verify
   that the agent reads them. Do not maintain a second architectural policy.

This repository's root `AGENTS.md` governs development of Smart Domain itself. A dependency download
or an upstream `AGENTS.md` does **not** automatically deliver instructions to a consumer's AI agent.

Record the artifact version and documentation tag/commit separately. Pin links to a tag or commit
that actually contains the selected documents, not moving `main`. A template added after a release
is not retroactively present in that release tag. Offline snapshots must retain upstream URL,
revision and retrieval date; review upgrades rather than silently editing the upstream contract.

The Pattern Contract is normative; guides, templates and demos explain it. The consumer's approved
requirements/ADRs supply project choices. If these conflict, stop the affected change and request an
explicit decision. Record any intentional deviation instead of claiming full pattern compliance.

## 3. Resolve Compatibility Before Adding Starters

Use the [compatibility matrix](./compatibility.md) before changing the host's dependencies.
A core-first adoption can preserve the existing HTTP framework while establishing the model.
The official API/MyBatis starter path requires its supported baseline or explicit integration evidence.
Do not silently replace the host's Spring Boot version, HTTP stack, database or build system.

For a Java library domain module, the minimal Gradle dependency is:

```gradle
plugins {
    id 'java-library'
}

repositories {
    mavenCentral()
}

dependencies {
    api platform('io.github.jayclock:smart-domain-bom:0.3.0')
    api 'io.github.jayclock:smart-domain-core'
}
```

Use the host's Java 17 toolchain configuration and test setup. Check dependency resolution and a
small consumer test before expanding integration. This snippet does not configure HTTP or a database.

## 4. Map Layers, Not A Service Pipeline

The following are responsibilities, not mandatory folder names or separately deployed services.
Map them onto the existing repository; create independent build modules when useful for enforcement.

| Layer | Owns | Must not own |
| --- | --- | --- |
| `bootstrap` | Startup, configuration, adapter/role-resolver wiring | Business decisions or a runtime business facade |
| `api` | Input conversion, root navigation, role switching, HAL and domain-error mapping | Mapper access or business policy |
| `domain` | Entities, Description values, roots, owner-defined associations, roles and invariants | HTTP, Spring, Jackson or MyBatis dependencies |
| `infrastructure/persistence` | Loading, hydration, batching, mapping, writes and concurrency mechanisms | Admission rules or business state transitions |
| `infrastructure/security` | Trusted actor authentication and identity integration | A replacement for domain role capabilities |
| `infrastructure/transaction` | Atomic execution and rollback | Repository-orchestration business services |

Compile-time dependencies:

```text
api --------------------> domain
infrastructure ---------> domain
bootstrap --------------> api + domain + infrastructure
```

Runtime business path:

```text
HTTP resource -> root association -> entity / context role
              -> owner-private wide association -> adapter
```

An `application` package is not required. An existing boundary may provide technical transaction
wrapping, but must not acquire domain decisions. Renaming a business Service to Store, Handler or
Manager does not fix ownership. Conversely, a technical helper is not wrong merely because its name
contains Service. Root associations can expose creation; the narrow/wide rule prevents callers from
mutating an entity-owned association behind its owner's back.

## 5. Design One Real Vertical Slice Before Coding

Fill the templates with the project's first approved scenario, not a fictional all-purpose model:

- roots and a connected object graph, with reasons for `Ref`, `HasOne`, `HasMany` or optional navigation;
- the association matrix and exact owner/field/wide-interface/adapter/API-rel correspondence;
- behavior owners, invariants, concrete acceptance data, failures and atomicity requirements;
- actor/context/role capabilities and denial behavior, or a reason roles are not applicable;
- transport compatibility, pagination/errors, idempotency and retry/concurrency expectations;
- business terminology (including agreed English/Chinese names) and measurable non-functional risks.

Draw a context/container view where external actors, stores or deployment boundaries need explanation.
It complements the association matrix; it cannot replace behavior ownership. Unknown infrastructure
and quality thresholds remain open decisions rather than invented defaults.

Then implement in this order:

1. Write domain behavior tests with association fakes, without HTTP or a database.
2. Implement behavior on entities/context roles and keep mutable owner associations private.
3. Implement production adapters behind the same contracts and run shared observable contract cases.
4. Wrap the **whole domain operation** in a technical transaction boundary. Test rollback and concurrent
   invariants against the actual storage. Separate mapper transactions are not enough for a multi-write action.
5. Project the same graph through API roots, links and operation affordances; preserve existing contracts.
6. Record tests, commands, results, remaining risks and review in the adoption checklist.

A fake can prove domain decisions, not SQL isolation or real authentication. Multiple databases,
filesystems or remote services need an explicit consistency/failure design; a local DB transaction
must not be presented as making all external side effects atomic.

## 6. Read The Existing Vertical Example

Do not maintain a second disconnected demo for this guide. Follow the accounting implementation:

| Concern | Existing reference |
| --- | --- |
| Business flow and owned associations | [Customer](../demo/src/main/java/reengineering/ddd/demo/accounting/model/Customer.java) |
| Context-specific behavior | [Bookkeeper](../demo/src/main/java/reengineering/ddd/demo/accounting/model/Bookkeeper.java) |
| Memory lifecycle | [InMemoryCustomers](../demo/src/main/java/reengineering/ddd/demo/accounting/memory/InMemoryCustomers.java) |
| Reference lifecycle | [AccountTransactions](../demo/src/main/java/reengineering/ddd/demo/accounting/mybatis/AccountTransactions.java) |
| HTTP projection | [AccountingApi](../demo/src/main/java/reengineering/ddd/demo/accounting/api/AccountingApi.java) |
| Domain and API evidence | [AccountingDemoTest](../demo/src/test/java/reengineering/ddd/demo/accounting/AccountingDemoTest.java), [AccountingApiTest](../demo/src/test/java/reengineering/ddd/demo/accounting/AccountingApiTest.java) |

Pair the examples with the [Anti-Patterns](./anti-patterns.md). Demo mapping/starter tests are not proof
of the consumer's production database, transaction isolation or security. Demo fixture identities are
not production actor resolution.

## 7. Make Compliance Observable

In the **Smart Domain source checkout**, these existing commands verify the library/demo and published
consumer samples:

```bash
./gradlew smartDomainCheck
./gradlew build publishToMavenLocal
./gradlew -p samples/consumer test
./gradlew -p samples/api-consumer test
```

`smartDomainCheck` checks the canonical accounting demo; it is not installed as a consumer-project
checker by a Maven dependency. Consumer samples resolve local publications. They do not prove that
an arbitrary consuming application is compliant or that an artifact was published to Maven Central.

The consumer must supply its own exact commands and evidence:

- automated dependency/API-surface checks for layer direction and exposed mutable associations;
- domain scenarios using fakes and the same association contract cases for production adapters;
- HTTP acceptance of business scenarios, navigation, errors and existing endpoint compatibility;
- risk-based usability (Q3) and security/concurrency/performance (Q4) evaluation alongside supporting
  technical (Q1) and business acceptance (Q2) tests;
- human review of behavior ownership: naming or import scans alone cannot recognize all business policy.

A checked template is not a test result. Finish only with observed results, explicit not-applicable
reasons and reviewed risks in the [adoption checklist](../templates/adoption-checklist.md).
