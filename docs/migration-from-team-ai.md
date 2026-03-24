# Migration From Team AI Modules

This guide maps existing Team AI internal modules to the publishable Smart Domain artifacts.

## Module Replacement

| Old Internal Module | New Artifact |
| --- | --- |
| `:smart-domain:core` | `smart-domain-core` |
| `:smart-domain:api-hateoas` | `smart-domain-api-hateoas` |
| `:smart-domain:api-jersey` | `smart-domain-api-jersey` |
| `:smart-domain:api-spring-boot-starter` | `smart-domain-api-spring-boot-starter` |
| `:smart-domain:persistence` | `smart-domain-persistence` |
| `:smart-domain:mybatis` | `smart-domain-mybatis` |
| `:smart-domain:mybatis-spring-boot-starter` | `smart-domain-mybatis-spring-boot-starter` |

## Package Replacement

| Old Package | New Package |
| --- | --- |
| `reengineering.ddd.archtype` | `io.github.jayclock.smartdomain.core` |
| `reengineering.ddd.teamai.api.VendorMediaType` | `io.github.jayclock.smartdomain.api.hateoas.media.VendorMediaType` |
| `reengineering.ddd.teamai.api.provider.VendorMediaTypeInterceptor` | `io.github.jayclock.smartdomain.api.jersey.VendorMediaTypeInterceptor` |
| `reengineering.ddd.teamai.api.Pagination` | `io.github.jayclock.smartdomain.api.hateoas.pagination.Pagination` |
| `reengineering.ddd.teamai.api.options.*` | `io.github.jayclock.smartdomain.api.hateoas.options.*` |
| `reengineering.ddd.teamai.api.schema.*` | `io.github.jayclock.smartdomain.api.hateoas.schema.*` |
| `reengineering.ddd.persistence.cache` | `io.github.jayclock.smartdomain.persistence` |
| `reengineering.ddd.mybatis.cache` | `io.github.jayclock.smartdomain.mybatis` |
| `reengineering.ddd.mybatis.database` | `io.github.jayclock.smartdomain.mybatis.database` |
| `reengineering.ddd.mybatis.memory` | `io.github.jayclock.smartdomain.mybatis.memory` |
| `reengineering.ddd.mybatis.support` | `io.github.jayclock.smartdomain.mybatis.support` |
| `reengineering.ddd.mybatis.autoconfigure` | `io.github.jayclock.smartdomain.boot` |

## Suggested Order

1. Import the BOM.
2. Replace package imports in your domain code with `smart-domain-core`.
3. Prefer `smart-domain-mybatis-spring-boot-starter` over low-level MyBatis modules unless you are
   composing the integration manually.
4. Prefer `smart-domain-api-spring-boot-starter` over low-level API modules unless you are
   composing the API stack manually.
5. Replace Spring config imports with `io.github.jayclock.smartdomain.boot.EnableSmartDomainMybatis`.
6. Run your context bootstrap and mapper tests against `mavenLocal` or a snapshot repository.

## Public Entry Points First

For most migrations, start from:

- `smart-domain-bom`
- `smart-domain-core`
- `smart-domain-api-spring-boot-starter`
- `smart-domain-mybatis-spring-boot-starter`

Only move down to `smart-domain-api-hateoas`, `smart-domain-api-jersey`, `smart-domain-persistence`
or `smart-domain-mybatis` when you intentionally need low-level composition.

## What Does Not Move

The following remain Team AI private:

- `reengineering.ddd.teamai.*`
- Team AI application resources and business-specific API models
- Team AI application bootstrapping and infrastructure wiring
