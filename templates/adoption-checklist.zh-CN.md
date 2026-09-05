# 接入验证模板

[English](./adoption-checklist.md) | 简体中文

复制到接入仓库、调整模板链接，并关联已填写的[关联矩阵](./association-matrix.zh-CN.md)和[上下文角色](./context-roles.zh-CN.md)。
这是证据记录，不表示检查已经存在或已经通过。

## 基线与决策

- 所选范围、需求及受保护的既有契约：`{{SCOPE_AND_CONTRACTS}}`
- 组件版本 / 依赖仓库 / 文档修订：`{{VERSION_SOURCE_DOCS}}`
- JDK、框架、构建工具、数据库及已选择的集成：`{{HOST_AND_INTEGRATIONS}}`
- 兼容性 ADR、备选方案、后果与回滚：`{{ADR}}`
- 设计记录、未决事项及审批负责人：`{{DESIGN_AND_OWNER}}`

实现前填完适用设计字段。不适用项明确说明理由；缺少环境意味着验证受阻，不自动等于不适用。

## 设计就绪

- [ ] 已约定目标、具体验收数据、术语、范围和非目标。
- [ ] 兼容性已评审；技术栈变更经过批准，而不是从 Demo 推断。
- [ ] 根关联、连通导航和关联矩阵完整。
- [ ] 拥有者的可变关联具有公共窄读取接口和私有宽接口字段。
- [ ] 每个不变量/状态转换都有拥有者实体或上下文角色，以及失败结果。
- [ ] 适用时明确角色能力、可信参与者解析和拒绝场景。
- [ ] 已定义模块依赖、API rel/affordance 映射和既有契约兼容性。
- [ ] 已设计原子操作、重试/幂等、并发不变量和外部失败。
- [ ] 已知精确项目命令、前置条件和基于风险的质量阈值。

## 计划检查与实际结果

每个可执行检查或人工工序占一行。状态为 `planned`、`passed`、`failed`、`blocked` 或 `not-applicable`。
执行后才填写观察结果，包括 commit、命令、退出码或评审结果、证据路径和日期。重跑时保留失败证据。

| ID / 行为或关联 | 目的 / 象限 | 被测对象及真实依赖与替身 | 工作目录 / 精确命令或工序 | 预期结果 | 状态 / 实际证据 | 负责人 / 不适用理由 |
| --- | --- | --- | --- | --- | --- | --- |
| CHECK-001 / `{{BEHAVIOR_IDS}}` | 领域决策 / Q1 | 领域 + 关联 fake | `{{DOMAIN_CHECK}}` | `{{INVARIANT_AND_FAILURE}}` | planned | `{{OWNER}}` |
| CHECK-002 / `{{ASSOCIATION_IDS}}` | 共享适配器契约 / Q1 | 每个选定适配器；标识真实存储 | `{{ADAPTER_CHECK}}` | `{{SAME_OBSERVABLE_CONTRACT}}` | planned | `{{OWNER}}` |
| CHECK-003 / `{{SCENARIO_IDS}}` | 业务验收 / Q2 | 所选 HTTP/运行时及依赖 | `{{ACCEPTANCE_CHECK}}` | `{{SCENARIO_AND_COMPATIBILITY}}` | planned | `{{OWNER}}` |
| CHECK-004 | 架构 / 构建 / Q1 | 接入项目源码与构建 | `{{QUALITY_CHECK}}` | `{{DEPENDENCIES_AND_BUILD}}` | planned | `{{OWNER}}` |
| CHECK-005 | 产品评价 / Q3 | 实际用户流程 | `{{USABILITY_PROCEDURE}}` | `{{CRITERIA}}` | planned | `{{OWNER}}` |
| CHECK-006 | 安全 / 可靠性 / 性能 / Q4 | 真实风险边界 | `{{RISK_CHECK}}` | `{{THRESHOLDS}}` | planned | `{{OWNER}}` |

象限描述目的，而不是固定测试框架。选择适用用例并按需加行；该表不强制选择数据库、浏览器工具或性能阈值。

## 证据覆盖

- [ ] 领域规则不依赖 HTTP/数据库即可运行，fake 不重新实现被测业务行为。
- [ ] 每个生产适配器运行相同的可观察关联契约，而不是较弱的复制版本。
- [ ] 所选真实数据库测试覆盖 hydration、分页、回滚和并发不变量。
- [ ] 所选 HTTP 测试覆盖根、链接、操作、媒体类型、错误和旧端点。
- [ ] 所选认证/安全检查覆盖真实 Token/身份处理与隔离。
- [ ] 依赖/API 表面检查约束分层方向，并防止公开可变关联。
- [ ] 人工评审了业务归属，不能只靠扫描证明 no-service 合规。
- [ ] 在接入项目执行了必需的测试/格式/构建检查，并记录实际结果。

Smart Domain 的 `./gradlew smartDomainCheck` 只检查其会计 Demo，不会随 Maven 依赖安装为接入项目的检查器。
上游构建或消费者示例通过，不能替代接入应用的证据。

## 评审与退出

| 剩余风险 / 偏离 | 影响 | 缓解 / 后续行动 | 评审人 / 决定 / 日期 |
| --- | --- | --- | --- |
| `{{RISK_OR_NONE}}` | `{{IMPACT}}` | `{{ACTION}}` | `{{REVIEW}}` |

- 必需检查全部通过，或明确报告剩余阻塞：`{{RESULT_SUMMARY}}`
- 行为归属，以及完整模式/部分集成结论：`{{COMPLIANCE_REVIEW}}`
- 项目批准记录与后续负责人：`{{APPROVAL}}`

必需检查受阻或失败时，不得宣称完成。已接受的偏离也必须保持可见；安装组件或填写表格不能证明完全遵循模式。
