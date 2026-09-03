# smart-domain-bom

[English](./README.md) | 简体中文

使用 BOM 让所有 Smart Domain 组件保持相同版本。

## 推荐用法

大多数用户从以下公共入口开始：

```groovy
implementation platform('io.github.jayclock:smart-domain-bom:0.3.0')
implementation 'io.github.jayclock:smart-domain-core'
implementation 'io.github.jayclock:smart-domain-api-spring-boot-starter'
implementation 'io.github.jayclock:smart-domain-mybatis-spring-boot-starter'
```

## Gradle 高级组合

```groovy
implementation platform('io.github.jayclock:smart-domain-bom:0.3.0')
implementation 'io.github.jayclock:smart-domain-core'
implementation 'io.github.jayclock:smart-domain-api-hateoas'
implementation 'io.github.jayclock:smart-domain-api-jersey'
implementation 'io.github.jayclock:smart-domain-api-spring-boot-starter'
implementation 'io.github.jayclock:smart-domain-mybatis'
implementation 'io.github.jayclock:smart-domain-mybatis-spring-boot-starter'
```

## Maven

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.jayclock</groupId>
      <artifactId>smart-domain-bom</artifactId>
      <version>0.3.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

BOM 统一管理：

- `smart-domain-core`
- `smart-domain-api-hateoas`
- `smart-domain-api-jersey`
- `smart-domain-api-spring-boot-starter`
- `smart-domain-persistence`
- `smart-domain-mybatis`
- `smart-domain-mybatis-spring-boot-starter`
