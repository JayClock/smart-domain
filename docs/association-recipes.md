# Association Recipes

These recipes are intentionally small. Use the accounting demo for the complete behavior and
wiring.

## Root association

A root association is the entry to the connected model. It is not a generic repository base class.

```java
public interface Customers {
  Customer create(CustomerDescription description, AccountSeed... accounts);

  Optional<Customer> findByIdentity(String identity);
}
```

The root exposes domain language and may be implemented by memory, database, or remote adapters.

## Mutable `HasMany`

Keep the mutable type inside the owner and return the narrow interface publicly.

```java
public final class Customer implements Entity<String, CustomerDescription> {
  private SourceEvidences sourceEvidences;

  public HasMany<String, SourceEvidence<?>> sourceEvidences() {
    return sourceEvidences;
  }

  public SourceEvidence<?> record(SourceEvidenceDescription description) {
    return sourceEvidences.add(description);
  }

  public interface SourceEvidences extends HasMany<String, SourceEvidence<?>> {
    SourceEvidence<?> add(SourceEvidenceDescription description);
  }
}
```

Callers can navigate `sourceEvidences()` but must use `Customer.record(...)` to modify it.

## Required `HasOne`

Use `HasOne` when the connection is guaranteed and the graph should navigate to the entity.

```java
public final class Transaction implements Entity<String, TransactionDescription> {
  private HasOne<Account> account;

  public Account account() {
    return account.get();
  }
}
```

Do not let `HasOne.get()` return `null`. Use a named optional association when absence is part of the
model.

## Identity fact with `Ref`

Use `Ref` inside descriptive value when the identity itself is the fact and loading the target is
not part of that value.

```java
public record SalesSettlementDescription(
    Ref<String> order, Ref<String> account, Amount total) {}
```

Add `HasOne` or `HasMany` separately if behavior or API navigation also needs the connected entity.

## Relation with its own meaning

When a connection has identity, role, status, dates, or behavior, model it as an entity.

```java
public final class Membership implements Entity<String, MembershipDescription> {
  private HasOne<User> user;
  private HasOne<Workspace> workspace;

  public User user() {
    return user.get();
  }

  public Workspace workspace() {
    return workspace.get();
  }
}
```

The owners then associate to `Membership`; they do not hide the role and dates in a join-table-only
adapter.

## Context role

Put actor/context-specific behavior on a role object.

```java
public interface Bookkeeper extends ContextRole<Operator, Customer> {
  default SourceEvidence<?> record(SourceEvidenceDescription description) {
    return context().record(description);
  }
}

public interface BookkeepingContext
    extends ContextSwitcher<Operator, Customer, Bookkeeper> {}
```

The resolver controls whether the actor can assume the role. The role exposes behavior without a
service-layer permission branch.

## Reference-lifecycle adapter

The adapter mirrors the owner and field and implements the wide interface.

```java
@AssociationMapping(entity = Account.class, field = "transactions", parentIdField = "accountId")
public final class AccountTransactions
    extends EntityList<String, Transaction>
    implements Account.Transactions {

  private String accountId;
  private AccountingLedgerMapper mapper;

  @Override
  protected List<Transaction> findEntities(int from, int to) {
    return mapper.findTransactionsByAccountId(accountId, from, to - from);
  }
}
```

It owns persistence mechanics, not account policy.

## Aggregated-lifecycle adapter

Use an in-memory association when associated entities move with the materialized owner.

```java
public final class SourceEvidenceTransactions
    extends io.github.jayclock.smartdomain.mybatis.memory.EntityList<String, Transaction>
    implements SourceEvidence.Transactions {}
```

The public domain contract remains `SourceEvidence.Transactions` regardless of lifecycle.

## HATEOAS projection

Start from a root and navigate the domain rather than calling a mapper or service.

```java
@Path("customers/{customerId}/source-evidences")
public final class SourceEvidencesApi {
  private final Customers customers;

  @GET
  @Path("{evidenceId}")
  public SourceEvidenceModel find(
      @PathParam("customerId") String customerId,
      @PathParam("evidenceId") String evidenceId) {
    Customer customer = customers.findByIdentity(customerId).orElseThrow(NotFound::new);
    SourceEvidence<?> evidence =
        customer.sourceEvidences().findByIdentity(evidenceId).orElseThrow(NotFound::new);
    return SourceEvidenceModel.of(customer, evidence);
  }
}
```

Represent connections with links and operations with affordances/templates so API navigation remains
aligned with the object graph.
