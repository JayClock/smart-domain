# Context Roles

Smart Domain supports context-specific role switching in `smart-domain-core`.

Use it when an actor enters a business context and gains a role object with behavior that only
exists inside that context.

## Core Types

- `ContextRole<Actor, Context>`
- `ContextRoleResolver<Actor, Context, Role>`
- `ContextSwitcher<Actor, Context, Role>`
- `ContextAccessDeniedException`

## Accounting Demo Mapping

The accounting demo uses:

- `Operator -> Customer -> Bookkeeper`
- `Operator -> Customer -> Auditor`
- `Operator -> Account -> Accountant`
- `Operator -> SourceEvidence -> EvidenceReviewer`

```java
public interface Bookkeeper extends ContextRole<Operator, Customer> {}

public interface BookkeepingContext
    extends ContextSwitcher<Operator, Customer, Bookkeeper> {}
```

The same shape is used for `Auditor`, `Accountant`, and `EvidenceReviewer`.

## Why It Exists

- It replaces ad hoc `if (role == ...)` checks with explicit role objects.
- It keeps context-aware behavior near the domain instead of pushing it into services.
- It lets one customer context expose multiple role-specific behaviors without inventing fake REST paths.

## Concrete Shape

In the accounting demo:

- `Bookkeeper.record(...)` records a `SalesSettlement` into a `Customer`
- `Auditor.transactions(accountId)` reads `Account.transactions()` inside the same customer context
- `Accountant.sourceEvidence(transactionId)` pivots from an `Account` context to the linked source evidence
- `EvidenceReviewer.settlementAccount()` pivots from a `SourceEvidence` context back to the posting account

That means your entity model stays entity-centric, while your behavior surface stays role-centric.
