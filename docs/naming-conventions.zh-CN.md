# 命名约定

[English](./naming-conventions.md) | 简体中文

当模型字段、宽接口、适配器和 Starter 包使用相同领域语言时，Smart Domain 最容易理解。

会计 Demo 使用以下对应关系：

```text
Account.transactions -> AccountTransactions -> AccountingLedgerMapper -> Starter
```

## 规则

- 关联字段使用领域语言，例如 `Account.transactions`；
- 适配器类名对应拥有者和字段，例如 `AccountTransactions`；
- Mapper 或持久化后端契约继续使用同一通用语言，例如 `AccountingLedgerMapper`；
- Starter 扫描根包保持一致，例如 `com.example.accounting.mybatis`。

## 示例映射

| 层 | 名称 |
| --- | --- |
| 领域实体字段 | `Account.transactions` |
| 宽接口 | `Account.Transactions` |
| 关联适配器 | `AccountTransactions` |
| Mapper 契约 | `AccountingLedgerMapper` |
| Starter 扫描根包 | `com.example.accounting.mybatis` |

## 上下文角色命名

上下文切换遵循相同规则：

- 上下文接口：`BookkeepingContext`
- 角色接口：`Bookkeeper`
- 上下文实现：`DefaultBookkeepingContext`

如果领域中已经有更准确的语言，不要使用 `ManagerContext` 或 `RoleAdapter` 等泛化名称。
