# Smart Domain Style Installation

This repository uses Smart Domain Pattern Contract v{{PATTERN_VERSION}}.

- Runtime baseline: `{{RUNTIME_VERSION}}`
- Installed from Smart Domain revision: `{{SOURCE_REVISION}}`
- Java base package: `{{BASE_PACKAGE}}`
- Portable skill: `.agents/skills/smart-domain-backend/`
- Agent contract: the managed Smart Domain block in `AGENTS.md`

## Runtime dependencies

Add only the modules the backend needs:

```gradle
dependencies {
    implementation platform("io.github.jayclock:smart-domain-bom:{{RUNTIME_VERSION}}")
    implementation "io.github.jayclock:smart-domain-core"

    // Optional MyBatis lifecycle adapters
    implementation "io.github.jayclock:smart-domain-mybatis-spring-boot-starter"

    // Optional Jersey/HATEOAS projection
    implementation "io.github.jayclock:smart-domain-api-spring-boot-starter"
}
```

## Start a feature

Ask the coding agent to use `smart-domain-backend` and first return:

1. acceptance scenarios with concrete identities, state, action, and outcome;
2. root associations;
3. the association matrix;
4. context roles;
5. invariant ownership;
6. Domain/Persistence/API/Test impact.

Confirm the graph before implementation. Then implement one scenario at a time in domain, adapter,
and API order. Do not insert an application service between those layers.

## Architecture gate

Run the dependency-free checker from the repository root:

```bash
python3 .smart-domain/check.py
```

Use `--strict-warnings` in CI after configured source roots exist. Edit `.smart-domain/config.json`
when modules or package names differ from the generated defaults. The checker rejects service/facade
orchestration, forbidden domain dependencies, inverted API/persistence imports, raw collections of
known entity types, and public leakage of wide association interfaces. If a `*Service` type is only
an external integration contract, list its fully qualified name under `allowedServiceTypes`.

For a Groovy Gradle build, it can be wired into `check` with:

```gradle
tasks.register('smartDomainCheck', Exec) {
    commandLine 'python3', '.smart-domain/check.py', '--strict-warnings'
}
tasks.named('check').configure { dependsOn tasks.named('smartDomainCheck') }
```

## Update the style kit

From a pinned Smart Domain checkout, rerun:

```bash
python3 tools/install-smart-domain-style.py /path/to/this/repository
```

The installer updates managed files and replaces only the marked Smart Domain block in
`AGENTS.md`; project-specific instructions outside that block are preserved.
