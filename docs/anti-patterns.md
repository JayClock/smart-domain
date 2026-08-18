# Smart Domain Anti-Patterns

Use these examples when reviewing human- or AI-generated code.

## Repository orchestration service

```java
class AccountingService {
  void record(Command command) {
    Customer customer = customers.find(command.customerId());
    Evidence evidence = evidences.save(command.evidence());
    transactions.saveAll(...);
    accounts.update(...);
  }
}
```

This disconnects the model and moves `Customer`/`Account` behavior into a service. Navigate from the
root, invoke `Customer.record(...)`, and let entity-owned associations hide storage.

## Raw entity collection

```java
class Customer {
  private List<Account> accounts;
}
```

A raw collection fixes loading and mutation semantics inside the entity. Use a named association
interface extending `HasMany`.

## Exposed wide interface

```java
public Customer.Accounts accounts() {
  return accounts;
}
```

If `Customer.Accounts` mutates state, callers can bypass `Customer` behavior. Return
`HasMany<String, Account>` and keep the wide field private.

## Generic CRUD association

```java
interface AssociationRepository<E> {
  E save(E entity);
  void delete(String id);
}
```

This erases domain language. Prefer `Customer.SourceEvidences.add`, `Customer.Accounts.update`, or
another owner/field-specific contract.

## Identifier hopping

```java
Account account = accountRepository.find(evidence.accountId());
```

When the conceptual model needs the connection, model it as `HasOne`/`HasMany` and navigate the
graph. Keep `Ref` for identity facts that intentionally do not provide navigation.

## Business rules in adapters

```java
class AccountTransactions {
  Transaction add(...) {
    if (!account.canPost(...)) throw ...;
    // write row
  }
}
```

The adapter may enforce storage constraints, but the business decision belongs to `Account` or a
context role. Adapters implement lifecycle mechanics.

## Controller-owned behavior

```java
@POST
Response record(Request request) {
  if (...) { /* business branch */ }
  mapper.insert(...);
}
```

Resources translate HTTP, enter through a root, switch roles, invoke domain behavior, and project the
result. They do not become services or persistence clients.

## Lifecycle-driven model changes

Creating different domain interfaces for memory and database versions of the same relation confuses
conceptual ownership with lifecycle. Keep one owner-defined association contract and swap adapters.

## Demo fixture presented as architecture

Seed-data helpers, deterministic IDs, and demo-only catalogs may make a sample runnable. Name and
document them as fixtures/bootstrap code. Do not route production behavior through a demo facade or
copy it as a service layer.
