# 消费者 AGENTS 模板

[English](./consumer-AGENTS.md) | 简体中文

这是模板，不会自动成为所有接入项目的有效指令。按[接入指南](../docs/adoption-guide.zh-CN.md)合并到宿主的 `AGENTS.md`，填写 `{{PLACEHOLDERS}}`，并为目标位置修复或移除这些模板链接。
不要覆盖宿主规则。其他 Agent 的入口应引用同一份指令，不建立分叉。

## 项目上下文与范围

- 项目及目的：`{{PROJECT_NAME_AND_PURPOSE}}`
- 当前任务、已批准需求和验收数据：`{{REQUIREMENTS_PATH}}`
- 非目标及不得变更的边界：`{{PROTECTED_BOUNDARIES}}`
- 现有 Java/框架/构建/数据库技术栈：`{{HOST_STACK}}`
- ADR 与审批负责人：`{{ADR_PATH_AND_OWNER}}`

## 版本化事实来源

- Smart Domain 组件版本与依赖仓库：`{{VERSION_AND_REPOSITORY}}`
- 上游文档 tag 或 commit：`{{DOCS_REF}}`
- 模式契约 URL 或保留来源信息的本地快照：`{{PATTERN_CONTRACT_LOCATION}}`
- 兼容性证据 / 待决策项：`{{COMPATIBILITY_RECORD}}`
- 项目分层/模块图与上下文/容器图：`{{ARCHITECTURE_PATH}}`
- 已填写的关联矩阵、不变量和 API 映射：`{{ASSOCIATION_MATRIX_PATH}}`
- 上下文角色与能力/拒绝规则：`{{CONTEXT_ROLES_PATH}}`
- 业务术语、命名、API、安全与审计规则：`{{PROJECT_RULES_PATH}}`
- 任务验证和接入证据：`{{ADOPTION_CHECKLIST_PATH}}`

文档应固定到确实包含引用文件的修订，不使用变化中的 `main`。库版本和文档修订可以不同，但要记录原因。
上游指令不会从依赖中自动加载。编码前显式读取上述来源。

模式契约是宣称 Smart Domain 合规的规范依据；项目需求和 ADR 定义具体选择。
来源冲突或缺失时，报告受影响决策并请求评审。不要静默覆盖宿主指令、虚构业务事实，或在差异未解决时宣称合规。

## 架构规则

- 遵循项目模块图：API 和基础设施依赖领域，组合根负责装配。
- 从根关联进入并导航实体/上下文角色。不要在业务 Service、Store、Handler 或 Facade 中通过仓储编排贫血实体。
- 在模型中共同表达标识、不可变 Description、关联和业务行为。
- `Ref` 表示标识事实；行为或 API 需要导航时使用可导航关联。
- 实体拥有的可变关联对外暴露窄读取 API；宽接口字段保持私有，写入经过拥有者行为。该规则不禁止根关联的创建操作。
- 不变量和业务转换属于实体或上下文角色。适配器实现生命周期、映射、存储及并发机制，不拥有业务策略。
- 参与者/上下文特有能力使用上下文切换；认证提供可信参与者，而不是由客户端选择权威。上下文角色不适用时记录理由。
- 领域不得依赖 Spring、HTTP、Jackson、MyBatis 或具体适配器。
- API 转换协议、导航根、切换角色并投影链接/affordance，不调用 Mapper 或重复领域决策。
- 技术性事务包围完整的原子领域操作，但不拥有其决策。非事务性外部副作用需另行定义失败/一致性行为。
- 未经过项目审批流程，不改变宿主技术栈或公共契约。
- 以职责而不是仅以类名判断辅助对象。Demo fixture 不是生产业务 Service。

## 实现行为前

1. 阅读已批准场景及相关模型/适配器/API 源码，不只看图。
2. 在设计中找到根、拥有者、关联、角色、不变量、失败及事务边界。
3. 使用关联 fake 编写聚焦领域测试，再最小实现和重构。
4. 为各个已选择的生产适配器运行同一组可观察契约用例，并验证真实存储。
5. 验证 HTTP 行为和兼容性，再执行下方任务与质量门禁。
6. 记录实际结果、剩余风险和行为归属评审。不要削弱测试或虚构成功；依赖/环境故障不是业务行为缺失的证据。

## 操作与验证

命令必须适用于当前仓库，填写工作目录与前置条件。不要把 Smart Domain 源码目录的命令当成由依赖自动安装到接入项目的能力。
不要在指令或日志中写入凭据和私有数据。

| 操作 / 门禁 | 工作目录 | 精确命令或人工工序 | 前提 / 预期结果 |
| --- | --- | --- | --- |
| 启动 / 健康检查 | `{{CWD}}` | `{{START_AND_HEALTH}}` | `{{ENV_AND_RESULT}}` |
| 数据库 / 迁移 | `{{CWD}}` | `{{DATABASE_PROCEDURE}}` | `{{DISPOSABLE_DB_OR_NA_REASON}}` |
| 浏览器 / API 调试 | `{{CWD}}` | `{{DEBUG_PROCEDURE}}` | `{{ENV_AND_RESULT}}` |
| 聚焦领域测试 | `{{CWD}}` | `{{DOMAIN_TEST_COMMAND}}` | `{{RESULT}}` |
| 适配器 / HTTP 验收 | `{{CWD}}` | `{{CONTRACT_COMMANDS}}` | `{{REAL_DEPENDENCIES}}` |
| 架构 / 格式 / 构建 | `{{CWD}}` | `{{QUALITY_COMMANDS}}` | `{{RESULT}}` |
| 风险评价 | `{{CWD}}` | `{{SECURITY_CONCURRENCY_USABILITY_PROCEDURES}}` | `{{REVIEWER_AND_THRESHOLDS}}` |

适用字段未填写时，阻止受影响范围的实现。不适用项要记录明确理由。
完成需要实际证据和项目批准，而不是只勾选清单。
