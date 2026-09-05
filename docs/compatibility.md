# Consumer Compatibility

English | [简体中文](./compatibility.zh-CN.md)

Scope: the `0.3.x` product line; concrete source/sample versions below describe `0.3.0`.
See [Release Readiness](./release-readiness.md) for release validation context and the
[Adoption Guide](./adoption-guide.md) for the migration sequence.

## Supported Baseline And Optional Integrations

| Surface | Baseline / source evidence | Consumer decision |
| --- | --- | --- |
| `smart-domain-core` | Java 17; no production dependencies in [core/build.gradle](../core/build.gradle) | Can be adopted first without installing HTTP or persistence starters |
| Official Spring Boot starters | Spring Boot 3.5.x; source BOM and API sample use 3.5.9 | Verify the host's resolved dependency graph and startup, not just imports |
| MyBatis integration | MyBatis Spring Boot Starter 3.0.x; the starter uses 3.0.4 | Add only when selecting MyBatis; configure datasource, mapper and association discovery |
| Official HTTP integration | Jersey, Spring HATEOAS, HAL/HAL-FORMS | Verify resource registration, media types, serialization, errors and existing routes |
| Other HTTP/storage adapters | Owner-defined domain contracts remain the extension point | Integration work is owned and tested by the consumer; it is not automatically an official starter configuration |
| Spring Boot 4.x | Outside the documented 0.3.x starter baseline | Treat as unverified, not as either proven compatible or proven impossible |
| JPA / code generation / multiple SQL dialects | No such integration or abstraction is included in 0.3.x | Do not infer support from the domain interfaces |

The [root build](../build.gradle), [API consumer build](../samples/api-consumer/build.gradle),
[MyBatis starter build](../mybatis-spring-boot-starter/build.gradle) and
[API starter build](../api-spring-boot-starter/build.gradle) are the executable version references.
A starter's transitive dependency graph is not the same as Core's dependency-free runtime surface.

## Choosing A Path

- **Supported starter path:** align with the supported baseline through an approved project decision,
  then follow the [MyBatis starter](../mybatis-spring-boot-starter/README.md) and
  [API quick start](../api-quick-start.md) as needed.
- **Core-first path:** retain the host stack and establish domain associations/roles first. Keeping MVC
  does not violate the domain pattern, but Core does not auto-configure MVC or provide the Jersey integration.
- **New baseline adaptation:** if the host cannot change and needs the full starters, isolate the
  compatibility work, test it, and record the supported combination. A forced version override or one
  successful compilation is not sufficient evidence.

Do not silently downgrade Spring Boot, switch MVC to Jersey, add a database or replace the host's
build system. Record context, alternatives, decision, consequences, owner and rollback in an ADR.
Select only needed artifacts; installing all modules does not establish architectural compliance.

## Version And Supply Policy

- Align artifacts through `smart-domain-bom` and pin a concrete release, not a dynamic version.
- Use Maven Central for a released version and check that the selected artifacts resolve there.
  A Git tag, CI result or `publishToMavenLocal` does not establish Central availability.
- Use local publication only for explicitly identified source/consumer verification. The repository's
  samples use `mavenLocal`; do not copy that preference into production builds unintentionally.
- Record the library version, documentation tag/commit, host JDK/Boot/MyBatis versions and dependency
  repository in the consumer's adoption record. The documentation revision must contain the files used.
- Prefer stable entrypoints. Record why any advanced API is needed; do not build against internal
  bootstrapping types merely because a library-internal test uses them.

## Required Compatibility Evidence

Before declaring an integration supported in the consumer, record:

1. Dependency resolution without accidental framework downgrades or mismatched Smart Domain modules.
2. Domain compilation and behavior tests on the selected JDK.
3. For HTTP: application startup, existing routes/health, HAL/media types, errors and actor handling.
4. For MyBatis: discovery/hydration, actual database reads and writes, pagination, transaction rollback
   and concurrent-invariant tests. A mocked mapper or reflection test does not prove SQL behavior.
5. Reproducible build/test commands in a clean environment, plus open limitations and rollback steps.

Mark non-selected integrations not applicable with reasons; do not invent passing results for them.
