# Smart Domain

Smart Domain extracts the reusable domain-modeling and MyBatis integration pieces from Team AI into
a small publishable product line.

## Product Layout

```text
smart-domain/
├── bom/
├── core/
├── persistence/
├── mybatis/
├── mybatis-spring-boot-starter/
├── demo/
└── samples/consumer/
```

The Gradle project paths stay stable as `:backend:*` during this refactor so existing internal
consumers do not need to change immediately. The product boundary is now expressed by the directory
layout instead of by `libs/backend/...`.

## What Problem It Solves

Smart Domain keeps business entities expressive without forcing eager collection loading or leaking
query logic into services.

The core pattern is:

- model one-to-many relationships as association objects instead of raw `List`
- keep entity behavior in domain types
- let persistence adapters load data lazily and in batches

## Modules

| Artifact | Purpose |
| --- | --- |
| `smart-domain-core` | Base entity and association abstractions |
| `smart-domain-persistence` | Cache hydration SPI and reflective base hydrator |
| `smart-domain-mybatis` | MyBatis-specific association adapters and hydrator |
| `smart-domain-mybatis-spring-boot-starter` | Spring Boot entry point |
| `smart-domain-bom` | Version alignment |

## Recommended Entry

For Spring Boot applications, start with:

1. `smart-domain-bom`
2. `smart-domain-core`
3. `smart-domain-mybatis`
4. `smart-domain-mybatis-spring-boot-starter`

If you are integrating without Spring Boot, start from `smart-domain-core` and add the persistence
module you actually need.

## Stable vs Internal API

Stable API:

- `io.github.jayclock.smartdomain.core.*`
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

## Next Reads

- [Starter README](./mybatis-spring-boot-starter/README.md)
- [BOM README](./bom/README.md)
- [Migration Guide](../docs/smart-domain/migration-from-team-ai.md)
- [Naming Conventions](../docs/smart-domain/naming-conventions.md)
