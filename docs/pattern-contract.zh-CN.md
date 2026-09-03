# Smart Domain 模式契约

[English](./pattern-contract.md) | 简体中文

状态：规范性文档

模式版本：1

本文档定义 Smart Domain 模块、示例和集成必须遵守的架构。教程可以补充说明，但不得削弱这些规则。

## 1. 无服务架构

Smart Domain 采用无服务领域架构。业务行为从以下三类对象之一进入：

1. 根关联；
2. 通过对象图到达的实体；
3. 绑定到参与者和领域上下文的上下文角色。

标准调用路径是：

```text
HTTP 资源
  -> 根关联
  -> 实体或 ContextRole
  -> 实体拥有的关联接口
  -> 关联适配器
```

类名包含 `Service` 并不一定错误，例如它可能表示外部系统集成。禁止的是应用/领域 `*Service`、用例处理器、命令处理器或门面：它们从仓储取出贫血实体，然后替实体做业务决策。

允许存在的辅助职责必须保持克制：

- 组合根负责装配实现；
- HTTP 资源负责解析请求、从根开始导航、调用领域行为并映射 HTTP/HATEOAS；
- 事务拦截器或工作单元负责提供原子性；
- 适配器负责加载和保存关联；
- 演示数据 fixture 负责初始化可运行示例。

这些对象都不能拥有领域决策。

## 2. 连通模型与根关联

应把概念领域建模为连通对象图，而不是通过标识符手工拼接的一组仓储。每个对象图至少包含一个根关联，例如 `Customers` 或 `Operators`，调用方从根关联进入模型。

实体由以下部分组成：

- 标识；
- 描述值；
- 指向其他实体的关联；
- 保护自身不变量并协调其所拥有的关联的行为。

数据库外键不会自动成为领域关联。只有当概念模型、业务行为或 API 需要遍历连接时，才增加可导航关联。

## 3. 选择关系形态

按以下顺序判断：

| 问题 | 建模方式 |
| --- | --- |
| 概念是否没有自己的标识？ | 放入 Description/值对象。 |
| 是否只记录另一个标识，并且不需要导航？ | `Ref<ID>` |
| 是否保证存在一个可导航实体？ | `HasOne<E>` |
| 是否存在零个或多个可导航实体？ | `HasMany<ID, E>` |
| 零或一个是否具有明确业务含义？ | 定义返回 `Optional<E>` 的命名关联。 |
| 关系本身是否具有标识、属性、角色或生命周期？ | 将关系建模为实体，再建立关联。 |

`Ref`、`HasOne` 和 `HasMany` 是互补关系。Description 可以把引用作为不可变业务事实保存；如果行为或 API 还需要导航，则另外提供关联。

## 4. 对外窄接口，对内宽接口

可变关联有两个视图：

- 供外部读取的窄接口；
- 由实体行为使用、由适配器实现的拥有者私有宽接口。

```java
public final class Account implements Entity<String, AccountDescription> {
  private Transactions transactions;

  public HasMany<String, Transaction> transactions() {
    return transactions;
  }

  public AccountChange record(
      SourceEvidence<?> evidence, List<TransactionDescription> descriptions) {
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

公共访问器不能返回 `Transactions`。否则调用方可以绕过 `Account` 行为，使关联适配器退化成公共 CRUD 服务。

如果关联只读且不需要更宽的契约，可以直接使用 `HasOne` 或 `HasMany`。

## 5. 行为归属

行为应放在能够使用领域语言表达规则的对象上：

- 实体行为负责保护不变量以及协调自身关联；
- 同一实体在不同参与者或上下文下能力不同时，使用上下文角色；
- 关联操作负责实体所需的生命周期机制，如 `add`、`update`、`append`、`remove` 或领域命名操作。

例如，`Customer.record(...)` 可以创建原始凭证、派生交易、通过 `Customer.accounts` 定位账户、调用 `Account` 行为，并通过宽关联接口持久化变更。如果把该流程拆到跨仓储 Service 中，就会丢失对象图，这不属于 Smart Domain。

如果某个行为看起来需要 Service，应先检查：

- 是否缺少根关联；
- 是否缺少实体关联；
- 行为是否放错了实体；
- 是否缺少上下文角色；
- 是否把本应导航对象图的流程写成了标识符跳转。

## 6. 上下文角色

当参与者进入上下文并获得相应行为时，使用 `ContextSwitcher<Actor, Context, Role>`。返回的 `ContextRole` 是领域对象，不是授权 DTO。

```java
Bookkeeper bookkeeper = bookkeepingContext.require(operator, customer);
bookkeeper.record(description);
```

解析器决定参与者能否承担该角色。角色方法暴露该上下文允许的行为，并委托给或协调连通实体。

## 7. 生命周期实现

概念所有权与持久化生命周期是两个独立决定。同一个概念模型可以混合：

- 根生命周期：入口关联定位根实体；
- 聚合生命周期：关联实体随拥有者一起物化，通常保存在内存中；
- 引用生命周期：适配器从数据库延迟或渐进加载实体；
- 远程生命周期：适配器从其他 API 获取关联实体；
- 投影生命周期：适配器从其他来源派生只读关联。

切换生命周期不能改变领域侧关联契约，具体选择由适配器隐藏。

## 8. 持久化对应关系

保持机械化命名对应：

```text
Owner.field
Owner.WideInterface
OwnerField 适配器
领域专用 Mapper/后端端口
```

例如：

```text
Account.transactions
Account.Transactions
AccountTransactions
AccountingLedgerMapper
```

对于 MyBatis 管理的关联，`@AssociationMapping` 必须准确指向拥有者字段和适配器中的父标识字段。适配器负责加载、批处理、映射、存储、乐观校验和基础设施异常转换，但不决定业务策略。

## 9. API 投影

REST/HATEOAS 投影同一个对象图：

- 根关联成为根资源；
- 实体成为实体资源；
- 关联成为子资源或链接关系；
- 关联实体通过该关系到达；
- 领域操作成为拥有者资源或关联上的 affordance/template。

HTTP 层可以把请求转换为 Description 或值对象、解析根实体、切换上下文角色、调用行为并映射领域失败，但不能调用 Mapper 或重复实现业务决策。

## 10. 必需的设计产物

实现前先写关联矩阵：

| 根 | 拥有者 | 字段 | 目标 | 基数 | 公共 API | 内部操作 | 生命周期 | 适配器 | API rel |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `Customers` | `Customer` | `accounts` | `Account` | 多 | `HasMany<String, Account>` | `update` | 根/聚合 | `CustomerAccounts` | `accounts` |
| `Customers` | `Account` | `transactions` | `Transaction` | 多 | `HasMany<String, Transaction>` | `add` | 引用 | `AccountTransactions` | `transactions` |

还应列出：

- 带具体数据的验收场景；
- 上下文切换和角色方法；
- 各实体拥有的不变量；
- 适配器契约测试；
- API 暴露的 rel 和 affordance。

只有这些产物形成一致对象图后才能开始实现。

## 11. 完成标准

Smart Domain 后端满足以下条件才算完成：

- 可以使用关联 fake 直接测试领域行为，不依赖 HTTP 或数据库；
- 每个生产适配器都满足同一份可观察关联契约；
- 不需要业务 Service 协调仓储；
- API 可以从根开始导航，并通过链接和 affordance 暴露同一个对象图；
- 生命周期可以在关联接口背后替换，而无需重新设计领域模型。
