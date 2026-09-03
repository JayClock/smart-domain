# 关联模式示例

[English](./association-recipes.md) | 简体中文

这些示例刻意保持简短。完整行为和装配方式请参考会计 Demo。

## 根关联

根关联是连通模型的入口，而不是通用仓储基类。

```java
public interface Customers {
  Customer create(CustomerDescription description, AccountSeed... accounts);

  Optional<Customer> findByIdentity(String identity);
}
```

根关联使用领域语言，可以由内存、数据库或远程适配器实现。

## 可变 `HasMany`

可变类型保留在拥有者内部，公共访问器只返回窄接口。

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

调用方可以通过 `sourceEvidences()` 导航，但修改必须经过 `Customer.record(...)`。

## 必需的 `HasOne`

当连接保证存在且对象图需要导航到该实体时，使用 `HasOne`。

```java
public final class Transaction implements Entity<String, TransactionDescription> {
  private HasOne<Account> account;

  public Account account() {
    return account.get();
  }
}
```

不要让 `HasOne.get()` 返回 `null`。如果缺失本身具有业务含义，应定义返回 `Optional<E>` 的命名关联。

## 使用 `Ref` 表达标识事实

当标识本身就是事实，而从该值加载目标对象并不属于其职责时，在描述值中使用 `Ref`。

```java
public record SalesSettlementDescription(
    Ref<String> order, Ref<String> account, Amount total) {}
```

如果业务行为或 API 同时需要导航到目标实体，应另外增加 `HasOne` 或 `HasMany`。

## 具有自身含义的关系

如果连接本身具有标识、角色、状态、日期或行为，应把它建模为实体。

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

拥有者关联到 `Membership`，不要只在数据库连接表中隐藏角色和日期。

## 上下文角色

将参与者/上下文特有行为放到角色对象中。

```java
public interface Bookkeeper extends ContextRole<Operator, Customer> {
  default SourceEvidence<?> record(SourceEvidenceDescription description) {
    return context().record(description);
  }
}

public interface BookkeepingContext
    extends ContextSwitcher<Operator, Customer, Bookkeeper> {}
```

解析器控制参与者能否承担角色。角色暴露适当行为，无需 Service 层权限分支。

## 引用生命周期适配器

适配器名称对应拥有者和字段，并实现宽接口。

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

它负责持久化机制，不负责账户业务策略。

## 聚合生命周期适配器

关联实体随已物化拥有者一起存在时，可以使用内存关联。

```java
public final class SourceEvidenceTransactions
    extends io.github.jayclock.smartdomain.mybatis.memory.EntityList<String, Transaction>
    implements SourceEvidence.Transactions {}
```

无论采用哪种生命周期，公共领域契约始终是 `SourceEvidence.Transactions`。

## HATEOAS 投影

从根关联进入并导航领域模型，而不是调用 Mapper 或 Service。

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

使用链接表示连接，使用 affordance/template 表示操作，使 API 导航与对象图保持一致。
