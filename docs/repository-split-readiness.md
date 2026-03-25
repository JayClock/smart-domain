# Smart Domain Repository Split Readiness

This checklist tracks the work needed to move `smart-domain/` into its own repository without
changing its public coordinates.

## Already Independent

- Product root build works from `smart-domain/`
- Product modules publish with `io.github.jayclock` coordinates
- API starter, Jersey integration, and external samples live under `smart-domain/`
- Publishing metadata is driven by `smartDomain*` Gradle properties

## Remaining Work Before Split

1. Decide the final repository URL and update `smartDomainProjectUrl` and SCM properties.
2. Add repository-level files to the future product repository:
   `LICENSE`, issue templates, release workflow, and Central publishing secrets.
3. Move or recreate the remaining product docs currently duplicated under `docs/smart-domain/`.
4. Add CI that runs from `smart-domain/` directly:
   `./gradlew build` and sample verification against published snapshots.
5. Publish a first external snapshot from the future repository namespace and verify downstream
   consumption outside this monorepo.

## Recommended Split Order

1. Freeze public coordinates and module names.
2. Create the standalone repository with the current `smart-domain/` tree as root.
3. Port CI, release secrets, and signing configuration.
4. Publish a new snapshot from the standalone repository.
5. Update Team AI to consume the published artifacts instead of sibling project references where
   appropriate.
