# smart-domain-mybatis-spring-boot-starter

[English](./README.md) | 简体中文

`smart-domain-mybatis-spring-boot-starter` 是 Spring Boot 应用接入 Smart Domain 持久化的推荐入口。

## Maven 坐标

```groovy
implementation platform('io.github.jayclock:smart-domain-bom:0.3.0')
implementation 'io.github.jayclock:smart-domain-core'
implementation 'io.github.jayclock:smart-domain-mybatis'
implementation 'io.github.jayclock:smart-domain-mybatis-spring-boot-starter'
implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.4'
```

## 最小配置

```java
@Configuration
@MapperScan("com.example.accounting.mybatis")
@EnableSmartDomainMybatis(
    associationBasePackages = "com.example.accounting.mybatis",
    leafEntityTypes = {Transaction.class})
class AccountingMybatisConfiguration {
  @Bean
  DataSource dataSource() { ... }

  @Bean
  SqlSessionFactory sqlSessionFactory(DataSource dataSource) { ... }
}
```

## 配置 `associationBasePackages`

该值应指向包含 `@AssociationMapping` 关联适配器的包根目录。

常见示例：

- `com.example.accounting.mybatis`
- `com.example.accounting.persistence.mybatis`

除非关联类确实分布在整个应用中，否则不要直接扫描应用根包。

## 配置 `leafEntityTypes`

列出本身不拥有其他关联对象，但仍需要被 Hydrator 识别的实体，例如：

- `Transaction.class`
- `SalesSettlement.class`
- `Operator.class`

如果实体始终可以通过 `@AssociationMapping` 发现，就不需要在 `leafEntityTypes` 中重复声明。

## 稳定 API

- `io.github.jayclock.smartdomain.boot.EnableSmartDomainMybatis`

## 内部 API

- `io.github.jayclock.smartdomain.boot.SmartDomainMybatisAutoConfiguration`
- `io.github.jayclock.smartdomain.boot.SmartDomainMybatisConfigurer`
- `io.github.jayclock.smartdomain.boot.SmartDomainMybatisRegistrar`

应用代码应依赖注解入口，而不是内部启动类型。
