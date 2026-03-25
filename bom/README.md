# smart-domain-bom

Use the BOM to keep every Smart Domain artifact on the same version.

## Recommended Public Usage

Most users should start with the public entrypoints:

```groovy
implementation platform('io.github.jayclock:smart-domain-bom:0.2.1')
implementation 'io.github.jayclock:smart-domain-core'
implementation 'io.github.jayclock:smart-domain-api-spring-boot-starter'
implementation 'io.github.jayclock:smart-domain-mybatis-spring-boot-starter'
```

## Gradle Advanced Composition

```groovy
implementation platform('io.github.jayclock:smart-domain-bom:0.2.1')
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
      <version>0.2.1</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

The BOM aligns:

- `smart-domain-core`
- `smart-domain-api-hateoas`
- `smart-domain-api-jersey`
- `smart-domain-api-spring-boot-starter`
- `smart-domain-persistence`
- `smart-domain-mybatis`
- `smart-domain-mybatis-spring-boot-starter`
