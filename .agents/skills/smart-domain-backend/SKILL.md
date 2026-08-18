---
name: smart-domain-backend
description: Design, generate, review, or refactor Java backends with Smart Domain's no-service association-object DDD pattern. Use whenever a request involves Smart Domain, association objects, HasOne/HasMany/Ref, connected domain graphs, context roles, MyBatis association adapters, HATEOAS projection, replacing service/repository orchestration, or creating a backend from domain scenarios—even when the user does not explicitly say "no service".
---
# Smart Domain Backend

Generate a connected, no-service domain model rather than a service layer over repositories.

## Load the contract

From this skill directory, read these repository files before planning:

1. `../../../docs/pattern-contract.md`
2. `../../../docs/association-recipes.md`
3. `../../../docs/anti-patterns.md`
4. `../../../demo/README.md`

Read the relevant accounting files named by the recipes when a concrete implementation shape is
needed. Treat the pattern contract as normative.

## Preserve the canonical call path

```text
HTTP resource
  -> root association
  -> entity or ContextRole
  -> owner-defined association interface
  -> lifecycle adapter
```

Do not generate an application service, use-case handler, mediator, facade, or repository
orchestrator that owns business behavior. Composition roots, transaction wrappers, protocol
translation, and demo fixtures may exist, but they must not make domain decisions.

## Phase 1: understand scenarios

Write acceptance scenarios with concrete identities, descriptions, starting state, action, and
observable outcome. Identify ambiguous business language before choosing classes or tables.

## Phase 2: model the connected graph

Always produce this table before code:

| Root | Owner | Field | Target | Cardinality | Public narrow API | Internal operations | Lifecycle | Adapter | API rel |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |

For each relation decide:

- value in Description when it has no identity;
- `Ref<ID>` when identity is a fact but navigation is intentionally absent;
- `HasOne<E>` for one guaranteed navigable entity;
- `HasMany<ID, E>` for a navigable collection;
- a named optional association for zero-or-one;
- a relation Entity when the relation has identity, attributes, roles, state, or lifecycle.

List every root association explicitly. If behavior needs to hop between repositories by ID, look
for a missing graph connection.

## Phase 3: assign behavior and roles

For every scenario name:

- the root entry;
- the navigation path;
- the entity method that owns each invariant and multi-association change;
- any `Actor -> Context -> ContextRole` switch and role method;
- the wide association operations needed internally.

Expose mutable associations narrowly:

```java
private Transactions transactions;

public HasMany<String, Transaction> transactions() {
  return transactions;
}

public interface Transactions extends HasMany<String, Transaction> {
  Transaction add(...);
}
```

Only entity or role behavior invokes `add`. External callers receive `HasMany`.

## Phase 4: implement scenario by scenario

Use this order:

1. descriptions and value objects;
2. root associations;
3. entities and narrow/wide association contracts;
4. context roles and resolvers;
5. direct domain tests with in-memory association fakes;
6. production memory/MyBatis/remote/projection adapters;
7. adapter contract tests;
8. JAX-RS/HATEOAS projection from roots;
9. rel, affordance, HAL-FORMS, and end-to-end tests.

Keep domain source free of Spring, MyBatis, JAX-RS, Jackson, and persistence imports.

## Lifecycle rules

Choose lifecycle only after the conceptual graph is stable:

- root lifecycle locates entry entities;
- aggregated lifecycle materializes connected entities with an owner;
- reference lifecycle loads progressively through an adapter;
- remote lifecycle follows another API;
- projection lifecycle derives a read-only relation.

Changing lifecycle must not change the owner-defined domain contract.

Name the implementation mechanically:

```text
Owner.field -> Owner.WideInterface -> OwnerField -> domain-specific mapper
```

For MyBatis, verify `@AssociationMapping.entity`, `field`, and `parentIdField` exactly.

## API rules

Resources parse protocol data, resolve roots, switch roles, invoke domain behavior, and build
representations. They do not call mappers or a business facade. Map associations to rels/subresources
and operations to affordances/templates so the API remains a projection of the graph.

## Verification

Prefer structural and behavioral assertions over exact generated text:

- no raw `List<Entity>` association fields;
- no service/repository orchestration;
- public mutable-association accessors are narrow;
- entity/context-role methods own behavior;
- adapters implement owner-defined wide interfaces;
- memory and production adapters satisfy equivalent contracts;
- API navigation begins at a root and exposes matching rels;
- domain imports remain framework-free.

Run the narrowest test first, then formatting and broader checks. Report commands and results.

## Final response

Report:

1. scenarios implemented;
2. association and context-role matrices;
3. files changed by domain, adapter, and API concern;
4. no-service design decisions;
5. tests and architecture checks;
6. assumptions and unresolved model risks.
