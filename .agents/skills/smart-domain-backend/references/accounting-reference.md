# Accounting Golden Reference

Use this compact model when the full Smart Domain repository is not available. It captures the
canonical no-service shape demonstrated by `demo/src/main/java/reengineering/ddd/demo/accounting`.

## End-to-end path

```text
AccountingApi
  -> Operators / Customers
  -> Operator + Customer
  -> BookkeepingContext / AuditContext
  -> Bookkeeper / Auditor
  -> Customer.SourceEvidences / Customer.Accounts
  -> association adapter
```

`AccountingApi` parses HTTP data, resolves roots, switches context, invokes a role, and builds HAL.
There is no accounting service or facade between the resource and the connected model.

## Association matrix

| Root | Owner | Field | Target | Narrow API | Wide operation | Lifecycle | Adapter name |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `Customers` | — | — | `Customer` | `HasMany` | root lookup/create | root | `InMemoryCustomers` or database root |
| `Operators` | — | — | `Operator` | `HasMany` | root lookup | root | `InMemoryOperators` |
| — | `Customer` | `accounts` | `Account` | `HasMany` | create/load | root/reference | `CustomerAccounts` |
| — | `Customer` | `sourceEvidences` | `SourceEvidence<?>` | `HasMany` | `add` | aggregated | `CustomerSourceEvidences` |
| — | `Account` | `transactions` | `Transaction` | `HasMany` | `add`/batch add | reference | `AccountTransactions` |
| — | `SourceEvidence<?>` | `transactions` | `Transaction` | `HasMany` | `add` | aggregated | `SourceEvidenceTransactions` |

## Context-role matrix

| Actor | Context | Subject | Role | Representative behavior |
| --- | --- | --- | --- | --- |
| `Operator` | `BookkeepingContext` | `Customer` | `Bookkeeper` | record source evidence and resulting transactions |
| `Operator` | `AuditContext` | `Customer` | `Auditor` | inspect customer accounting evidence |
| `Operator` | `AccountContext` | `Account` | `Accountant` | work with account transactions |
| `Operator` | `EvidenceReviewContext` | `SourceEvidence<?>` | `EvidenceReviewer` | inspect evidence transactions |

## Narrow-in, wide-inside shape

```java
public final class Account implements Entity<String, AccountDescription> {
  private Transactions transactions;

  public HasMany<String, Transaction> transactions() {
    return transactions;
  }

  public interface Transactions extends HasMany<String, Transaction> {
    void add(Transaction transaction);
  }
}
```

The adapter implements `Account.Transactions`. Callers only receive `HasMany`. An entity or context
role invokes the wide operation while protecting invariants.

## Multi-association behavior

The accounting flow records evidence and derived transactions as one domain action. The owning
entity/role coordinates its already-connected associations; it does not expose repositories to an
orchestration service.

```text
Bookkeeper.record(description)
  -> Customer.record(description)
  -> Customer.SourceEvidences.add(evidence)
  -> evidence creates transaction descriptions
  -> Account.Transactions.add(transaction)
  -> balances and evidence links remain consistent
```

## Adapter mapping

A production adapter is named mechanically from owner and field:

```text
Account.transactions
  -> Account.Transactions
  -> AccountTransactions
```

For MyBatis, its `@AssociationMapping` identifies the owning entity, exact injected field, and
parent identity field. The adapter handles loading, batching, mapping, and storage—not accounting
rules.

## Tests

The golden test stack is:

1. direct entity/context-role tests using association fakes;
2. equivalent memory and MyBatis association contract tests;
3. HTTP tests with root/context fakes and HAL assertions;
4. architecture checks preventing service/facade drift and framework imports in the model.
