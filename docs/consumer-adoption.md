# Adopt Smart Domain In Another Repository

Smart Domain has two independently versioned concerns:

1. runtime modules imported through the BOM;
2. Pattern Contract v1, installed as coding-agent instructions and bundled references.

Importing the runtime alone does not enforce the architecture. Install both.

## 1. Pin a Smart Domain release

Use a release tag or commit rather than an unpinned `main` checkout. The installed manifest records
the source revision, runtime version, and pattern version so reviews can identify the exact baseline.

## 2. Install the style kit

From the pinned Smart Domain checkout, run:

```bash
python3 tools/install-smart-domain-style.py /path/to/consumer-repository
```

The installer creates or updates:

```text
consumer-repository/
├── .agents/skills/smart-domain-backend/
│   ├── SKILL.md
│   ├── evals/evals.json
│   └── references/
├── .smart-domain/
│   ├── README.md
│   └── manifest.json
└── AGENTS.md                    # managed Smart Domain block only
```

The complete skill is portable: its Pattern Contract, recipes, anti-patterns, and compact Accounting
reference are bundled under `references/`. The consumer repository does not need a Smart Domain
submodule or sibling checkout after installation.

The installer is idempotent. On update, it replaces the installed skill and only the content between
these markers in `AGENTS.md`:

```text
<!-- smart-domain-style:start -->
<!-- smart-domain-style:end -->
```

Instructions outside the markers are preserved. It refuses to overwrite an existing unmanaged
skill or `.smart-domain/README.md`; inspect the conflict and use `--force` only when replacement is
intentional.

## 3. Import runtime modules

For Gradle:

```gradle
dependencies {
    implementation platform("io.github.jayclock:smart-domain-bom:${smartDomainVersion}")
    implementation "io.github.jayclock:smart-domain-core"

    // Add only when needed.
    implementation "io.github.jayclock:smart-domain-mybatis-spring-boot-starter"
    implementation "io.github.jayclock:smart-domain-api-spring-boot-starter"
}
```

A consumer using custom persistence or HTTP infrastructure can depend on `smart-domain-core` only.
See [Getting Started](../getting-started.md) for Maven coordinates and module-level imports.

## 4. Start with a graph plan

Ask the coding agent to use `smart-domain-backend` and not write production code until it returns:

1. acceptance scenarios with concrete data;
2. root associations;
3. an association matrix;
4. context-role switches;
5. invariant ownership;
6. Domain/Persistence/API/Test impact.

The accepted implementation path is:

```text
HTTP resource
  -> root association
  -> entity or ContextRole
  -> owner-defined association interface
  -> persistence adapter
```

Implement one scenario at a time in this order:

```text
domain graph and behavior
  -> association fakes and direct domain tests
  -> production adapters and adapter contract tests
  -> HTTP/HATEOAS projection
  -> architecture and end-to-end checks
```

## 5. Update intentionally

Review Pattern Contract changes before updating consumers. Then rerun the installer from the new
pinned release and commit the skill, manifest, generated README, and managed `AGENTS.md` block in the
consumer repository.

Do not copy `.pi/agents` or `.pi/prompts` blindly. They coordinate work inside the Smart Domain
source repository. Consumer repositories normally need only the portable skill and their managed
`AGENTS.md` contract.
