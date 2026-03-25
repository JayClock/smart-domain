# Smart Domain 0.2.1 Release Readiness

## Current Snapshot State

As of 2026-03-24, the repository has produced and validated a local snapshot release for:

- `io.github.jayclock:smart-domain-core:0.2.1`
- `io.github.jayclock:smart-domain-api-hateoas:0.2.1`
- `io.github.jayclock:smart-domain-api-jersey:0.2.1`
- `io.github.jayclock:smart-domain-api-spring-boot-starter:0.2.1`
- `io.github.jayclock:smart-domain-api-model-tree-tool:0.2.1`
- `io.github.jayclock:smart-domain-persistence:0.2.1`
- `io.github.jayclock:smart-domain-mybatis:0.2.1`
- `io.github.jayclock:smart-domain-mybatis-spring-boot-starter:0.2.1`
- `io.github.jayclock:smart-domain-bom:0.2.1`

## Public Product Surface

The recommended public entrypoints for `0.1.x` are:

- `io.github.jayclock:smart-domain-bom`
- `io.github.jayclock:smart-domain-core`
- `io.github.jayclock:smart-domain-api-spring-boot-starter`
- `io.github.jayclock:smart-domain-mybatis-spring-boot-starter`

The following modules remain published for advanced composition, but are not the primary adoption
path:

- `smart-domain-api-hateoas`
- `smart-domain-api-jersey`
- `smart-domain-persistence`
- `smart-domain-mybatis`

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
