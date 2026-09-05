# 关联与行为设计模板

[English](./association-matrix.md) | 简体中文

复制到接入仓库，在实现所选场景前填写，并为目标位置调整模板链接。
遵循[模式契约](../docs/pattern-contract.zh-CN.md)，配合[上下文角色](./context-roles.zh-CN.md)和[验证记录](./adoption-checklist.zh-CN.md)使用。
下方行是占位符，不是已批准的业务模型。

## 范围与追踪

- 项目 / 所选场景 / 具体验收数据：`{{PROJECT_SCENARIO_DATA}}`
- 需求及上下文/容器图：`{{REQUIREMENTS_AND_DIAGRAM_PATHS}}`
- Smart Domain 版本 / 文档修订：`{{VERSION_AND_DOCS_REF}}`
- 决策负责人 / 评审状态 / 待澄清问题：`{{REVIEW_AND_QUESTIONS}}`

## 连通对象图

`{{ROOTS_OWNERS_NAVIGATION_AND_CONTEXT_BOUNDARIES}}`

每个对象图至少描述一个根。说明 `Ref` 标识事实与导航的区别；仅在目标保证存在时使用 `HasOne`，有业务意义的缺失用返回 `Optional` 的命名关联。
具有自身标识、属性或生命周期的关系建模为实体。不要把每个外键都变为导航，也不要在模型需要关联时通过 ID 跳转。

## 关联矩阵

使用稳定的项目 ID。显式标识只读关联；根创建可以是根关联操作。

| ID | 根 | 拥有者 | 字段 | 目标 | 基数 | 公共读取 API | 内部宽操作 | 生命周期 | 适配器 | API rel |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ASSOC-001 | `{{ROOT}}` | `{{OWNER}}` | `{{FIELD}}` | `{{TARGET}}` | `{{CARDINALITY}}` | `{{READ_API}}` | `{{WIDE_INTERFACE_AND_OPERATIONS}}` | `{{LIFECYCLE}}` | `{{ADAPTER}}` | `{{REL_OR_NA}}` |

保持 `Owner.field → Owner.WideInterface → OwnerField adapter` 对应。
记录为什么选择根、聚合、引用、远程或投影生命周期；改变生命周期不应改变领域契约。
列出每个计划中的 fake/生产实现，并区分 fixture 与持久存储。

## 行为、不变量与原子性

| ID | 场景 | 根 / 拥有者实体或角色方法 | 前置条件 / 不变量 | 关联 ID | 失败 / 不变状态 | 原子性 / 并发场景 |
| --- | --- | --- | --- | --- | --- | --- |
| BEH-001 | `{{SCENARIO}}` | `{{DOMAIN_ENTRY}}` | `{{RULE}}` | ASSOC-001 | `{{FAILURE}}` | `{{ATOMICITY}}` |

按适用范围为每个操作说明事务边界、乐观/锁机制、重试/幂等和外部副作用失败处理。
业务策略属于拥有者/角色；适配器执行存储和并发机制。
将每个不变量映射到测试，包括顺序 fake 无法证明的竞争条件。

## API 投影与兼容性

| 操作 | 领域导航 / 行为 | 方法 / 路径 / rel / affordance | 输入 Description 或值对象 | 输出 / 分页 | 错误 / 已有契约 |
| --- | --- | --- | --- | --- | --- |
| `{{OPERATION}}` | BEH-001 / ASSOC-001 | `{{HTTP_CONTRACT}}` | `{{INPUT}}` | `{{OUTPUT}}` | `{{ERROR_AND_COMPATIBILITY}}` |

记录哪些公共关联可导航，以及如何暴露允许的操作。未选择 HTTP 表面时说明原因，不虚构端点。
公共表示不包含服务端路径、凭据或内部持久化类型。

## 项目术语与非功能约束

| 英文术语 / 标识符 | 中文术语 | 含义 / 拥有者 | 单位、格式或禁用同义词 |
| --- | --- | --- | --- |
| `{{TERM}}` | `{{CHINESE_TERM}}` | `{{MEANING}}` | `{{CONSTRAINT}}` |

记录适用的授权、审计、隐私、多语言、性能和可运维约束，包含测量方法、负责人及证据路径：`{{QUALITY_CONSTRAINTS}}`。
未知阈值保留为问题，不视为已批准。实现前链接填写后的接入检查清单。
