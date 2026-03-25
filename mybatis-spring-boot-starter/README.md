# smart-domain-mybatis-spring-boot-starter

`smart-domain-mybatis-spring-boot-starter` is the recommended entry point for Spring Boot
applications.

This is one of the primary public entrypoints for Smart Domain persistence adoption.

## Coordinates

```groovy
implementation platform('io.github.jayclock:smart-domain-bom:0.2.1')
implementation 'io.github.jayclock:smart-domain-core'
implementation 'io.github.jayclock:smart-domain-mybatis'
implementation 'io.github.jayclock:smart-domain-mybatis-spring-boot-starter'
implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.4'
```

## Minimal Configuration

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

## How To Fill `associationBasePackages`

Point `associationBasePackages` at the package root that contains your association adapters
annotated with `@AssociationMapping`.

Typical values:

- `com.example.accounting.mybatis`
- `com.example.accounting.persistence.mybatis`

Do not point it at your entire application root unless your association classes actually live
there.

## How To Fill `leafEntityTypes`

List the entity classes that do not own association objects but still need to be recognized by the
hydrator.

Typical examples:

- `Transaction.class`
- `SalesSettlement.class`
- `Operator.class`

If an entity is only ever reached through `@AssociationMapping` discovery, it does not need to be
repeated in `leafEntityTypes`.

## Stable API

- `io.github.jayclock.smartdomain.boot.EnableSmartDomainMybatis`

## Internal API

- `io.github.jayclock.smartdomain.boot.SmartDomainMybatisAutoConfiguration`
- `io.github.jayclock.smartdomain.boot.SmartDomainMybatisConfigurer`
- `io.github.jayclock.smartdomain.boot.SmartDomainMybatisRegistrar`

Application code should depend on the annotation entry point, not on the internal bootstrapping
types.
