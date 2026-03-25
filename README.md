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

- Group: `io.github.jayclock`
- Version: `0.2.1`

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
| `smart-domain-bom` | Public version-alignment entrypoint |
| `smart-domain-core` | Public core entrypoint for entity, association, and context-role abstractions |
| `smart-domain-api-spring-boot-starter` | Public API entrypoint for Spring Boot applications |
| `smart-domain-api-model-tree-tool` | External utility for building recursive JSON link trees from Java API model source files |
| `smart-domain-mybatis-spring-boot-starter` | Public persistence entrypoint for Spring Boot applications |
| `smart-domain-api-hateoas` | Advanced low-level API support module |
| `smart-domain-api-jersey` | Advanced low-level Jersey integration module |
| `smart-domain-persistence` | Advanced low-level hydration SPI |
| `smart-domain-mybatis` | Advanced low-level MyBatis integration module |

## Public Entry Points

Most users should start with only these artifacts:

1. `smart-domain-bom`
2. `smart-domain-core`
3. `smart-domain-api-spring-boot-starter` when you expose Smart Domain resources over REST
4. `smart-domain-mybatis-spring-boot-starter` when you integrate Smart Domain with MyBatis

This is the supported product surface we intend external users to adopt first.

## Advanced Modules

The remaining artifacts are still published, but they are low-level composition modules rather than
the primary product entrypoints:

- `smart-domain-api-hateoas`
- `smart-domain-api-jersey`
- `smart-domain-persistence`
- `smart-domain-mybatis`

Use them only when you are intentionally composing Smart Domain without the starters, such as:

- building without Spring Boot
- integrating only the HATEOAS layer
- integrating only the MyBatis layer
- extending Smart Domain internals in framework-specific ways

## Stable vs Internal API

Stable API:

- `io.github.jayclock.smartdomain.core.*`
- `io.github.jayclock.smartdomain.core.context.*`
- `io.github.jayclock.smartdomain.api.jersey.VendorMediaTypeInterceptor`
- `io.github.jayclock.smartdomain.boot.SmartDomainApiAutoConfiguration`
- `io.github.jayclock.smartdomain.boot.SmartDomainApiJerseyAutoConfiguration`
- `io.github.jayclock.smartdomain.boot.SmartDomainApiProperties`
- `io.github.jayclock.smartdomain.boot.EnableSmartDomainMybatis`

Internal API:

- Types annotated with `io.github.jayclock.smartdomain.core.InternalApi`
- Bootstrapping helpers and cache serialization types

Advanced module API:

- `io.github.jayclock.smartdomain.api.hateoas.*`
- `io.github.jayclock.smartdomain.persistence.EntityHydrator`
- `io.github.jayclock.smartdomain.persistence.AbstractReflectiveEntityHydrator`
- `io.github.jayclock.smartdomain.persistence.HydratingCacheManager`
- `io.github.jayclock.smartdomain.mybatis.AssociationMapping`
- `io.github.jayclock.smartdomain.mybatis.GenericEntityHydrator`
- `io.github.jayclock.smartdomain.mybatis.database.EntityList`

## Docs

- [Release Readiness](./docs/release-readiness.md)
- [Releasing](./RELEASING.md)
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
