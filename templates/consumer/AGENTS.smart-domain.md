<!-- smart-domain-style:start -->
## Smart Domain implementation contract

This repository follows Smart Domain Pattern Contract v1. Before planning or editing backend code,
read:

1. `.agents/skills/smart-domain-backend/references/pattern-contract.md`
2. `.agents/skills/smart-domain-backend/references/association-recipes.md`
3. `.agents/skills/smart-domain-backend/references/anti-patterns.md`
4. `.agents/skills/smart-domain-backend/references/accounting-reference.md`

The canonical production call path is:

```text
HTTP resource
  -> root association
  -> entity or ContextRole
  -> owner-defined association interface
  -> persistence adapter
```

Do not introduce an application/domain service, use-case handler, mediator, facade, command handler,
or repository orchestration layer that owns business behavior. Composition roots may wire objects,
resources may translate protocols, and infrastructure may provide transactions; none owns domain
decisions.

Before implementation, produce acceptance scenarios with concrete data, root associations, an
association matrix, a context-role matrix when actors vary by context, and invariant ownership.
Then implement in this order:

```text
domain graph and behavior
  -> association fakes and direct domain tests
  -> production association adapters and contract tests
  -> HTTP/HATEOAS graph projection
  -> architecture and end-to-end checks
```

Domain code must not import Spring, MyBatis, JAX-RS, Jackson, API, or persistence types. Model
navigable entity relations with `HasOne`/`HasMany`, identity-only facts with `Ref<ID>`, and expose
mutable associations narrowly while adapters implement owner-defined wide interfaces.

Run the consumer architecture gate before declaring backend work complete:

```bash
python3 .smart-domain/check.py
```
<!-- smart-domain-style:end -->
