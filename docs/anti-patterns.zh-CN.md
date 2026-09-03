# Smart Domain 反模式

[English](./anti-patterns.md) | 简体中文

审查 Smart Domain 实现时，可以使用以下反例。

## 仓储编排 Service

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

这种写法切断对象图，把 `Customer`/`Account` 的行为移到 Service。正确方式是从根开始导航，调用 `Customer.record(...)`，并让实体拥有的关联隐藏存储细节。

## 原始实体集合

```java
class Customer {
  private List<Account> accounts;
}
```

原始集合把加载和修改语义固定在实体内部。应使用继承 `HasMany` 的命名关联接口。

## 暴露宽接口

```java
public Customer.Accounts accounts() {
  return accounts;
}
```

如果 `Customer.Accounts` 可以修改状态，调用方就能绕过 `Customer` 行为。公共访问器应返回 `HasMany<String, Account>`，宽接口字段保持私有。

## 通用 CRUD 关联

```java
interface AssociationRepository<E> {
  E save(E entity);
  void delete(String id);
}
```

这种接口会抹去领域语言。应使用 `Customer.SourceEvidences.add`、`Customer.Accounts.update` 或其他拥有者/字段专用契约。

## 通过标识符跳转

```java
Account account = accountRepository.find(evidence.accountId());
```

如果概念模型需要该连接，应使用 `HasOne`/`HasMany` 并导航对象图。只有明确不提供导航的标识事实才使用 `Ref`。

## 在适配器中实现业务规则

```java
class AccountTransactions {
  Transaction add(...) {
    if (!account.canPost(...)) throw ...;
    // 写入数据库
  }
}
```

适配器可以强制存储约束，但业务决策属于 `Account` 或上下文角色。适配器只实现生命周期机制。

## Controller 拥有业务行为

```java
@POST
Response record(Request request) {
  if (...) { /* 业务分支 */ }
  mapper.insert(...);
}
```

资源负责转换 HTTP、从根进入、切换角色、调用领域行为并投影结果；不能成为 Service 或持久化客户端。

## 因生命周期而修改领域模型

为同一关系的内存版和数据库版创建不同领域接口，会把概念所有权和生命周期混为一谈。应保留一个由拥有者定义的关联契约，仅替换适配器。

## 把 Demo Fixture 当成架构

种子数据工具、确定性 ID 和演示目录可以让示例运行，但应明确命名并说明它们属于 fixture/启动代码。生产业务不能通过演示门面调用，也不应把它复制成 Service 层。
