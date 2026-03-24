# smart-domain-bom

Use the BOM to keep every Smart Domain artifact on the same version.

## Gradle

```groovy
implementation platform('io.github.jayclock.smartdomain:smart-domain-bom:0.1.0-SNAPSHOT')
implementation 'io.github.jayclock.smartdomain:smart-domain-core'
implementation 'io.github.jayclock.smartdomain:smart-domain-api-hateoas'
implementation 'io.github.jayclock.smartdomain:smart-domain-api-jersey'
implementation 'io.github.jayclock.smartdomain:smart-domain-api-spring-boot-starter'
implementation 'io.github.jayclock.smartdomain:smart-domain-mybatis'
implementation 'io.github.jayclock.smartdomain:smart-domain-mybatis-spring-boot-starter'
```

## Maven

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.jayclock.smartdomain</groupId>
      <artifactId>smart-domain-bom</artifactId>
      <version>0.1.0-SNAPSHOT</version>
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
