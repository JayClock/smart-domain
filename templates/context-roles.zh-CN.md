# 上下文角色设计模板

[English](./context-roles.md) | 简体中文

在接入仓库中与[关联矩阵](./association-matrix.zh-CN.md)一起填写，复制后调整模板链接。
先阅读框架的[上下文角色](../docs/context-roles.zh-CN.md)和[模式契约](../docs/pattern-contract.zh-CN.md)，不要把角色名称当作授权 DTO。

## 适用性与可信参与者

- 场景及评审负责人：`{{SCENARIO_AND_OWNER}}`
- 是否存在上下文特有能力 / 不适用及理由：`{{APPLICABILITY}}`
- 认证如何建立参与者，以及由哪个基础设施负责：`{{TRUSTED_ACTOR_SOURCE}}`
- 上下文查找、隔离边界与成员/指派来源：`{{CONTEXT_AND_ASSIGNMENTS}}`

不要把请求中的角色或用户 ID 当成权威。解析器决定已认证参与者能否在实际上下文中承担角色。
缺少成员关系、能力被拒绝、角色撤销和上下文不存在都需要明确结果；通过 HTTP 暴露时需要一致的隐私策略。

## 参与者 → 上下文 → 角色

| ID | 参与者 | 上下文 | Switcher / 解析器 | ContextRole 类型 | 能力方法 | 领域行为 | 准入 / 拒绝 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| ROLE-001 | `{{ACTOR}}` | `{{CONTEXT}}` | `{{SWITCHER_AND_RESOLVER}}` | `{{ROLE}}` | `{{CAPABILITIES}}` | `{{BEHAVIOR_IDS}}` | `{{ADMISSION_AND_DENIAL}}` |

适用时使用 `ContextSwitcher<Actor, Context, Role>` 和 `ContextRole<Actor, Context>`。
分别记录解析器的领域决策、加载指派关系的适配器和启动装配。
角色方法委托给或协调连通实体，不拼接断开的仓储，也不把领域策略移入安全过滤器。

## 角色验收与风险场景

| 场景 | 具体参与者 / 上下文 / 指派关系 | 调用能力 | 预期领域结果 / 不变状态 | 测试 / 证据 |
| --- | --- | --- | --- | --- |
| 允许 | `{{ALLOWED_DATA}}` | `{{METHOD}}` | `{{SUCCESS}}` | `{{TEST}}` |
| 拒绝 | `{{DENIED_DATA}}` | `{{METHOD}}` | `{{DENIAL}}` | `{{TEST}}` |
| 跨上下文访问 | `{{OTHER_CONTEXT_DATA}}` | `{{METHOD}}` | `{{ISOLATION}}` | `{{TEST}}` |
| 指派撤销或变化 | `{{REVOCATION_DATA}}` | `{{METHOD}}` | `{{REVOCATION_POLICY}}` | `{{TEST}}` |

说明并发下角色解析与修改如何交互、是否重新检查指派，以及哪些审计事件排除秘密数据：`{{CONCURRENCY_AND_AUDIT}}`。
不需要的场景标记不适用并说明理由。领域角色测试不能证明真实身份提供商或 Token 校验；在[接入检查清单](./adoption-checklist.zh-CN.md)中链接选定的集成/安全检查。
