# Smart Domain Pattern Contract

Status: normative  
Pattern version: 1

This document defines the architecture that Smart Domain examples, AI instructions, and generated
backends must follow. Tutorials may elaborate on it but must not weaken it.

## 1. No-service architecture

Smart Domain is a no-service domain architecture. Business behavior enters through one of three
objects:

1. a root association;
2. an entity reached through the connected model;
3. a context role bound to an actor and a domain context.

The canonical flow is:

```text
HTTP resource
  -> root association
  -> entity or ContextRole
  -> entity-owned association interface
  -> association adapter
```

A class is not forbidden merely because it integrates with an external service. What is forbidden
is an application/domain `*Service`, use-case handler, command handler, or facade that retrieves
anemic entities and performs their business decisions for them.

Allowed supporting responsibilities are deliberately narrow:

- a composition root wires implementations;
- an HTTP resource parses requests, navigates from a root, invokes domain behavior, and maps the
  result to HTTP/HATEOAS;
- a transaction interceptor or unit-of-work wrapper supplies atomic infrastructure;
- an adapter loads and stores an association;
- a demo-data fixture seeds runnable examples.

None of these owns domain decisions.

## 2. Connected model and root associations

Model the conceptual domain as a connected object graph rather than a set of repositories joined
manually by identifiers. Every graph has at least one root association, such as `Customers` or
`Operators`, through which callers enter the model.

An entity consists of:

- identity;
- descriptive value;
- associations to other entities;
- behavior that protects its invariants and coordinates its owned associations.

A database foreign key is not automatically a domain association. Add a navigable association when
the conceptual model, behavior, or API needs to traverse that connection.

## 3. Choosing the relation shape

Use this decision sequence:

| Question | Model |
| --- | --- |
| Does the concept have no identity of its own? | Put it in a Description/value object. |
| Is only another identity recorded as a fact, with no navigation? | `Ref<ID>` |
| Is exactly one connected entity guaranteed? | `HasOne<E>` |
| Are zero or more connected entities available? | `HasMany<ID, E>` |
| Is zero-or-one meaningful? | Define a named association exposing an `Optional<E>` read API. |
| Does the relation itself have identity, attributes, roles, or lifecycle? | Model the relation as an Entity, then associate to it. |

`Ref`, `HasOne`, and `HasMany` are complementary. A Description may retain a reference as part of an
immutable business fact while an association supplies graph navigation where required.

## 4. Narrow outside, wide inside

A mutable association has two views:

- the public narrow API used for reading;
- the owner-private wide interface used by entity behavior and implemented by an adapter.

```java
public final class Account implements Entity<String, AccountDescription> {
  private Transactions transactions;

  public HasMany<String, Transaction> transactions() {
    return transactions;
  }

  public AccountChange record(
      SourceEvidence<?> evidence, List<TransactionDescription> descriptions) {
    // Domain behavior invokes the wide field internally.
    return new AccountChange(
        Amount.sum(
            descriptions.stream()
                .map(description -> transactions.add(this, evidence, description))
                .map(transaction -> transaction.getDescription().amount())
                .toArray(Amount[]::new)));
  }

  public interface Transactions extends HasMany<String, Transaction> {
    Transaction add(
        Account account, SourceEvidence<?> evidence, TransactionDescription description);
  }
}
```

Do not return `Transactions` from the public accessor. Doing so lets callers bypass `Account`
behavior and turns the association adapter into a public CRUD service.

A read-only association may use `HasOne` or `HasMany` directly when no wider contract is needed.

## 5. Behavior placement

Put behavior on the object that can state the rule in domain language:

- entity behavior for invariant-preserving state changes and coordination of its associations;
- context-role behavior when the same entity offers different capabilities to different actors or
  contexts;
- association operations for lifecycle mechanics required by the entity (`add`, `update`,
  `append`, `remove`, and domain-named variants).

For example, `Customer.record(...)` may create source evidence, derive transactions, resolve
accounts through `Customer.accounts`, invoke `Account` behavior, and persist changes through the
wide association interfaces. Splitting that flow across repositories in a service loses the object
graph and is not Smart Domain.

If a behavior appears to need a service, first look for:

- a missing root association;
- a missing entity association;
- behavior assigned to the wrong entity;
- a missing context role;
- an identifier-based workflow that should navigate the graph.

## 6. Context roles

Use `ContextSwitcher<Actor, Context, Role>` when an actor acquires behavior inside a context. The
resulting `ContextRole` is a domain object, not an authorization DTO.

```java
Bookkeeper bookkeeper = bookkeepingContext.require(operator, customer);
bookkeeper.record(description);
```

Resolvers decide whether the role can be assumed. Role methods expose behavior appropriate to that
context and delegate to or coordinate the connected entities.

## 7. Lifecycle implementations

Conceptual ownership and persistence lifecycle are separate decisions. One conceptual model may
mix:

- root lifecycle — an entry association locates root entities;
- aggregated lifecycle — associated entities are materialized with the owner, often in memory;
- reference lifecycle — an adapter loads entities lazily or progressively from a database;
- remote lifecycle — an adapter obtains connected entities from another API;
- projection lifecycle — an adapter derives a read-only association from another source.

Changing lifecycle must not change the domain-facing association contract. The adapter hides the
choice.

## 8. Persistence correspondence

Keep a mechanical naming correspondence:

```text
Owner.field
Owner.WideInterface
OwnerField adapter
Domain-specific mapper/backing port
```

Example:

```text
Account.transactions
Account.Transactions
AccountTransactions
AccountingLedgerMapper
```

For MyBatis-managed associations, `@AssociationMapping` must point to the exact owner field and the
adapter's parent identity field. Adapters perform loading, batching, mapping, storage, optimistic
checks, and infrastructure error translation. They do not decide business policy.

## 9. API projection

REST/HATEOAS projects the same graph:

- a root association becomes a root resource;
- an entity becomes an entity resource;
- an association becomes a subresource or link relation;
- a connected entity becomes a resource reached through that association;
- a domain operation becomes an affordance/template on the owning resource or association.

The HTTP layer may translate a request into a Description or value object, resolve the root entity,
switch context roles, invoke behavior, and map domain failures. It must not call mappers or
reimplement business decisions.

## 10. Required planning artifact

Before generating code, write an association matrix:

| Root | Owner | Field | Target | Cardinality | Public API | Internal operations | Lifecycle | Adapter | API rel |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `Customers` | `Customer` | `accounts` | `Account` | many | `HasMany<String, Account>` | `update` | root/aggregated | `CustomerAccounts` | `accounts` |
| `Customers` | `Account` | `transactions` | `Transaction` | many | `HasMany<String, Transaction>` | `add` | reference | `AccountTransactions` | `transactions` |

Also list:

- acceptance scenarios and concrete data;
- context switches and role methods;
- invariants owned by each entity;
- adapter contract tests;
- rels and affordances exposed by the API.

Code generation starts only after these artifacts form one coherent graph.

## 11. Completion criteria

A Smart Domain backend is complete when:

- domain behavior can be tested directly with association fakes and no HTTP/database;
- each production adapter satisfies the same observable association contract;
- no business service is needed to coordinate repositories;
- the API can navigate from a root and expose the same graph through links and affordances;
- lifecycle choices can change behind association interfaces without redesigning the domain.
