# Naming Conventions

Smart Domain works best when the model field, wide interface, adapter, and starter package all use
the same domain language.

The accounting demo uses this correspondence:

`Account.transactions -> AccountTransactions -> AccountingLedgerMapper -> Starter`

## Rule

- Association field names stay in the domain language: `Account.transactions`.
- The adapter class mirrors the owner and field: `AccountTransactions`.
- The mapper or backing persistence contract stays in the same ubiquitous language: `AccountingLedgerMapper`.
- The starter scan root follows the same package: `com.example.accounting.mybatis`.

## Example Mapping

| Layer | Name |
| --- | --- |
| Domain entity field | `Account.transactions` |
| Wide interface | `Account.Transactions` |
| Association adapter | `AccountTransactions` |
| Mapper contract | `AccountingLedgerMapper` |
| Starter scan root | `com.example.accounting.mybatis` |

## Context Role Naming

Use the same rule for context switching:

- context interface: `BookkeepingContext`
- role interface: `Bookkeeper`
- context implementation: `DefaultBookkeepingContext`

Do not mix generic names like `ManagerContext` or `RoleAdapter` if the domain already has better
language.
