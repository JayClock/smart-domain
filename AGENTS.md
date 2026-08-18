# Smart Domain Repository Guide

Smart Domain is a Java 17 product line and executable reference for association-object DDD. The
canonical accounting model is intentionally **no-service**: business behavior starts from a root
association, an entity, or a context role and continues through association objects.

## Read order

Before changing production code or generating a backend, read:

1. `docs/pattern-contract.md` — normative Smart Domain rules.
2. `docs/association-recipes.md` — copyable association shapes.
3. `docs/anti-patterns.md` — designs that must not be generated.
4. `demo/README.md` and the accounting model — executable reference.
5. The README for each affected infrastructure/API module.

Tutorials and the historical PDF explain the motivation; `docs/pattern-contract.md` wins if wording
conflicts.

## Canonical call path

```text
HTTP resource
  -> root association
  -> entity or ContextRole
  -> entity-owned wide association
  -> memory/database/remote adapter
```

Do not introduce an application service, use-case handler, mediator, or repository orchestration
layer between these steps. A composition root may wire objects, an HTTP resource may translate the
protocol, and infrastructure may wrap the call in a transaction. None of those may own business
decisions.

## Domain modeling rules

- Model a connected object graph and identify at least one root association.
- Keep identity and descriptive value in `Entity`; model navigable connections with `HasOne` and
  `HasMany`.
- Use `Ref<ID>` for an identity fact that does not itself provide navigation.
- Never represent an entity association as a raw mutable `List`.
- For a mutable association, keep the wide interface next to the owning entity and expose only the
  narrow read interface to callers.
- Put multi-association behavior in the entity that owns the behavior, as `Customer.record(...)`
  does in the accounting demo.
- Put actor/context-specific behavior in a `ContextRole`, resolved by a `ContextSwitcher`.
- Treat aggregated, reference, root, and remote lifecycles as adapter choices, not as changes to the
  conceptual model.
- Name adapters by owner and field: `Account.transactions` -> `Account.Transactions` ->
  `AccountTransactions`.

## Generation workflow

Before writing code, produce:

1. acceptance scenarios with concrete data;
2. an association matrix containing root, owner, field, target, cardinality, public narrow API,
   internal mutations, lifecycle, adapter, and API rel;
3. a context-role matrix when actors behave differently by context;
4. invariants and observable outcomes.

Then implement in this order:

1. descriptions, entities, root associations, association interfaces, and context roles;
2. direct domain tests using in-memory association fakes;
3. production association adapters and adapter contract tests;
4. JAX-RS/HATEOAS projection of the same graph;
5. rel/template and end-to-end tests.

Do not start from tables, controllers, DTOs, or generic CRUD repositories.

## Boundaries

- Domain/model code must not import Spring, MyBatis, JAX-RS, Jackson, or persistence classes.
- Adapters implement domain-owned interfaces; they do not define domain contracts or rules.
- API resources do not call mappers and do not rebuild a second business model.
- Context access checks belong in context resolvers and roles, not scattered conditionals.
- Keep `.pi` agents, portable skills, docs, and the accounting demo aligned with the normative
  contract.

## Verification

Use the narrowest relevant command first:

```bash
./gradlew smartDomainCheck
./gradlew :core:test
./gradlew :demo:test
./gradlew :demo:spotlessCheck
./gradlew check
```

For domain behavior, test against association fakes. For adapters, verify the same observable
association contract against memory and production implementations. For API changes, verify media
types, `_links`, `_templates`, affordances, and rel-based navigation.
