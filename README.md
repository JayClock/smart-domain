# Smart Domain

Smart Domain is a publishable Java product line for association-object based domain modeling,
progressive-loading persistence, and HATEOAS-first API exposure.

## Product Layout

```text
smart-domain/
├── bom/
├── core/
├── api-hateoas/
├── api-jersey/
├── api-spring-boot-starter/
├── persistence/
├── mybatis/
├── mybatis-spring-boot-starter/
├── demo/
├── samples/consumer/
└── samples/api-consumer/
```

The product now builds from `smart-domain/` directly and can be split into its own repository
without changing artifact coordinates.

## Build From Product Root

```bash
cd smart-domain
./gradlew build
./gradlew publishToMavenLocal
./gradlew -p samples/consumer test
./gradlew -p samples/api-consumer test
```

Coordinates:

- Group: `io.github.jayclock.smartdomain`
- Version: `0.1.0-SNAPSHOT`

## What Problem It Solves

Smart Domain keeps business entities expressive without forcing eager collection loading or leaking
query logic into services.

The core pattern is:

- model one-to-many relationships as association objects instead of raw `List`
- model context-specific behavior as role objects instead of permission checks scattered in services
- keep entity behavior in domain types
- let persistence adapters load data lazily and in batches

## Modules

| Artifact | Purpose |
| --- | --- |
| `smart-domain-core` | Base entity, association, and context-role switching abstractions |
| `smart-domain-api-hateoas` | Reusable vendor media type, pagination, HAL-FORMS and JSON Schema support |
| `smart-domain-api-jersey` | Jersey-specific interceptor and integration support |
| `smart-domain-api-spring-boot-starter` | Spring Boot auto-configuration for Smart Domain API support |
| `smart-domain-persistence` | Cache hydration SPI and reflective base hydrator |
| `smart-domain-mybatis` | MyBatis-specific association adapters and hydrator |
| `smart-domain-mybatis-spring-boot-starter` | Spring Boot entry point |
| `smart-domain-bom` | Version alignment |

## Recommended Entry

For Spring Boot applications, start with:

1. `smart-domain-bom`
2. `smart-domain-core`
3. `smart-domain-api-hateoas` if you are exposing Smart Domain resources over REST or HAL
4. `smart-domain-api-jersey` if you are building on Jersey directly
5. `smart-domain-api-spring-boot-starter` if you want Spring Boot to auto-configure HAL and Jersey support
6. `smart-domain-mybatis`
7. `smart-domain-mybatis-spring-boot-starter`

If you are integrating without Spring Boot, start from `smart-domain-core` and add the persistence
module you actually need.

## Stable vs Internal API

Stable API:

- `io.github.jayclock.smartdomain.core.*`
- `io.github.jayclock.smartdomain.core.context.*`
- `io.github.jayclock.smartdomain.api.hateoas.*`
- `io.github.jayclock.smartdomain.api.jersey.VendorMediaTypeInterceptor`
- `io.github.jayclock.smartdomain.boot.SmartDomainApiAutoConfiguration`
- `io.github.jayclock.smartdomain.boot.SmartDomainApiJerseyAutoConfiguration`
- `io.github.jayclock.smartdomain.boot.SmartDomainApiProperties`
- `io.github.jayclock.smartdomain.persistence.EntityHydrator`
- `io.github.jayclock.smartdomain.persistence.AbstractReflectiveEntityHydrator`
- `io.github.jayclock.smartdomain.persistence.HydratingCacheManager`
- `io.github.jayclock.smartdomain.mybatis.AssociationMapping`
- `io.github.jayclock.smartdomain.mybatis.GenericEntityHydrator`
- `io.github.jayclock.smartdomain.mybatis.database.EntityList`
- `io.github.jayclock.smartdomain.boot.EnableSmartDomainMybatis`

Internal API:

- Types annotated with `io.github.jayclock.smartdomain.core.InternalApi`
- Bootstrapping helpers and cache serialization types

## Docs

- [Release Readiness](./docs/release-readiness.md)
- [Repository Split Readiness](./docs/repository-split-readiness.md)
- [Migration Guide](./docs/migration-from-team-ai.md)
- [Naming Conventions](./docs/naming-conventions.md)
- [Context Roles](./docs/context-roles.md)
- [Starter README](./mybatis-spring-boot-starter/README.md)
- [API Quick Start](./api-quick-start.md)
- [API Jersey README](./api-jersey/README.md)
- [API Starter README](./api-spring-boot-starter/README.md)
- [API Consumer Sample](./samples/api-consumer/README.md)
- [BOM README](./bom/README.md)
