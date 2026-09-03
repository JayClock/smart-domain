# 上下文角色

[English](./context-roles.md) | 简体中文

`smart-domain-core` 支持上下文相关的角色切换。当参与者进入某个业务上下文，并获得只在该上下文中存在的行为时，可以使用该机制。

## 核心类型

- `ContextRole<Actor, Context>`
- `ContextRoleResolver<Actor, Context, Role>`
- `ContextSwitcher<Actor, Context, Role>`
- `ContextAccessDeniedException`

## 会计 Demo 映射

会计示例包含：

- `Operator -> Customer -> Bookkeeper`
- `Operator -> Customer -> Auditor`
- `Operator -> Account -> Accountant`
- `Operator -> SourceEvidence -> EvidenceReviewer`

```java
public interface Bookkeeper extends ContextRole<Operator, Customer> {}

public interface BookkeepingContext
    extends ContextSwitcher<Operator, Customer, Bookkeeper> {}
```

`Auditor`、`Accountant` 和 `EvidenceReviewer` 使用同一形态。

## 设计目的

- 用明确角色对象取代零散的 `if (role == ...)` 判断；
- 让上下文相关行为靠近领域，而不是进入 Service；
- 允许同一个客户上下文暴露多个角色能力，而不必创造虚假的 REST 路径。

## 具体行为

会计 Demo 中：

- `Bookkeeper.record(...)` 为 `Customer` 记录 `SalesSettlement`；
- `Auditor.transactions(accountId)` 在客户上下文内读取 `Account.transactions()`；
- `Accountant.sourceEvidence(transactionId)` 从账户上下文跳转到关联原始凭证；
- `EvidenceReviewer.settlementAccount()` 从原始凭证上下文返回入账账户。

这样可以让实体模型保持以实体为中心，同时让行为接口保持以角色为中心。
