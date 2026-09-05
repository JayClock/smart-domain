# Repository Guidelines

## Scope And Required Reading

This file governs development of **Smart Domain itself**, not every application importing its artifacts.
For a consuming project, start with the [Adoption Guide](./docs/adoption-guide.md) and merge the
[consumer AGENTS template](./templates/consumer-AGENTS.md) into that project's own instructions.

Before changing a model, adapter, API or example, read the [Pattern Contract](./docs/pattern-contract.md),
[Association Recipes](./docs/association-recipes.md), [Anti-Patterns](./docs/anti-patterns.md) and the
relevant module/source/tests. Read [compatibility](./docs/compatibility.md) before changing dependencies.

## Architecture And Verification

- Organize code in layers; execute business behavior through roots, entities/context roles and owned associations.
- Keep domain decisions out of HTTP resources, persistence adapters and technical transaction wrappers.
- Keep entity-owned wide association fields private and expose narrow read APIs; root creation is allowed.
- Preserve model/framework boundaries and owner/field/adapter correspondence. A renamed Store is not a fix for a business Service.
- Treat the Pattern Contract as normative. Keep guides/templates synchronized; record conflicts explicitly.
- Keep consumer requirements, filled design records and environment-specific commands in consumer repositories.
- New public behavior needs tests; dependency/internal-API changes need consumer verification. Do not infer
  runtime compatibility or complete compliance from a successful import or a naming scan.

Run from this repository root as applicable:

```bash
./gradlew smartDomainCheck
./gradlew build publishToMavenLocal
./gradlew -p samples/consumer test
./gradlew -p samples/api-consumer test
```

`smartDomainCheck` checks the canonical demo, not arbitrary consumers. For documentation-only changes,
verify bilingual counterparts, template placeholder parity, code/command consistency and local links.
Report actual checks and blockers; do not present documentation or unchecked boxes as execution evidence.

## Bilingual documentation

- Every maintained Markdown document must have both English and Simplified Chinese versions.
- Use `name.md` for English and `name.zh-CN.md` for Simplified Chinese.
- Each language version must link to its counterpart near the top of the document.
- Add, update, rename, and delete both versions in the same change.
- Keep code examples, commands, artifact coordinates, versions, links, and technical meaning synchronized.
- `AGENTS.md` and `RELEASING.md` are single-file exceptions and do not require separate Chinese files.
- Before committing documentation changes, verify bilingual counterparts and local links.

## 双语文档

- 每一份持续维护的 Markdown 文档都必须同时提供英文和简体中文版本。
- 英文文件使用 `name.md`，简体中文文件使用 `name.zh-CN.md`。
- 每个语言版本都必须在文档顶部附近链接到对应版本。
- 新增、更新、重命名或删除文档时，必须在同一次变更中同步处理两个版本。
- 两个版本中的代码示例、命令、组件坐标、版本号、链接和技术含义必须保持一致。
- `AGENTS.md` 和 `RELEASING.md` 是单文件例外，不需要单独的中文文件。
- 提交文档变更前，检查双语版本和本地链接。

## 作用域与必读资料

本文件约束 **Smart Domain 本身的开发**，不自动约束所有引入组件的应用。
接入项目先阅读[接入指南](./docs/adoption-guide.zh-CN.md)，再将[消费者 AGENTS 模板](./templates/consumer-AGENTS.zh-CN.md)合并到自己的指令中。

修改模型、适配器、API 或示例前，阅读[模式契约](./docs/pattern-contract.zh-CN.md)、[关联模式示例](./docs/association-recipes.zh-CN.md)、[反模式](./docs/anti-patterns.zh-CN.md)以及相关模块/源码/测试。
修改依赖前阅读[兼容性](./docs/compatibility.zh-CN.md)。

## 架构与验证

- 按分层组织代码，通过根、实体/上下文角色和拥有的关联执行业务。
- HTTP 资源、持久化适配器和技术性事务包装不拥有领域决策。
- 实体拥有的宽关联字段保持私有，对外暴露窄读取 API；允许根创建操作。
- 保持领域/框架边界及拥有者/字段/适配器对应。将业务 Service 改名为 Store 不解决问题。
- 模式契约是规范依据，指南/模板需同步；冲突应显式记录。
- 消费者需求、填写后的设计记录和环境特有命令保留在消费者仓库中。
- 新公共行为需要测试；依赖/内部 API 变更需要消费者验证。不能从 import 成功或名称扫描推断运行时兼容或完全合规。

按适用范围在本仓库根目录运行上方四条 Gradle 命令。
`smartDomainCheck` 检查标准 Demo，不检查任意接入项目。
纯文档变更需检查双语对应、模板占位符一致性、代码/命令一致性及本地链接。
报告实际检查和阻塞，不把文档或未执行的清单当作运行证据。
