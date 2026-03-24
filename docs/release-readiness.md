# Smart Domain 0.1.0 Release Readiness

## Current Snapshot State

As of 2026-03-24, the repository has produced and validated a local snapshot release for:

- `io.github.jayclock.smartdomain:smart-domain-core:0.1.0-SNAPSHOT`
- `io.github.jayclock.smartdomain:smart-domain-api-hateoas:0.1.0-SNAPSHOT`
- `io.github.jayclock.smartdomain:smart-domain-api-jersey:0.1.0-SNAPSHOT`
- `io.github.jayclock.smartdomain:smart-domain-api-spring-boot-starter:0.1.0-SNAPSHOT`
- `io.github.jayclock.smartdomain:smart-domain-persistence:0.1.0-SNAPSHOT`
- `io.github.jayclock.smartdomain:smart-domain-mybatis:0.1.0-SNAPSHOT`
- `io.github.jayclock.smartdomain:smart-domain-mybatis-spring-boot-starter:0.1.0-SNAPSHOT`
- `io.github.jayclock.smartdomain:smart-domain-bom:0.1.0-SNAPSHOT`

## Validated Commands

Snapshot publication from the product root:

```bash
cd smart-domain
./gradlew \
  :core:publishToMavenLocal \
  :api-hateoas:publishToMavenLocal \
  :api-jersey:publishToMavenLocal \
  :api-spring-boot-starter:publishToMavenLocal \
  :persistence:publishToMavenLocal \
  :mybatis:publishToMavenLocal \
  :mybatis-spring-boot-starter:publishToMavenLocal \
  :bom:publishToMavenLocal
```

External consumer verification:

```bash
cd smart-domain
./gradlew -p samples/consumer test
./gradlew -p samples/api-consumer test
```

## Known Limitations For 0.1.x

- Supported stack is limited to Java 17, Spring Boot 3.5.x, and MyBatis Spring Boot Starter 3.0.x.
- Validation has been completed against `mavenLocal`, not Maven Central.
- No signing, staging, or Central release automation has been configured yet.
- No JPA integration, code generator, or multi-dialect abstraction is included in `0.1.x`.
