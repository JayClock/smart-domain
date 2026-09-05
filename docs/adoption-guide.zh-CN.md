# 消费者接入指南

[English](./adoption-guide.md) | 简体中文

**按分层组织代码，按连通领域对象图执行业务。**
本指南把规范性[模式契约](./pattern-contract.zh-CN.md)转化为接入流程，不引入 Controller → Service → Repository 架构，也不要求使用全部组件。

## 1. 区分框架规则与项目决策

| 前馈信息 | Smart Domain 提供 | 接入仓库补充 |
| --- | --- | --- |
| Why：项目上下文 | 适用范围、约束、模式契约和阅读顺序 | 业务目标、现有栈、范围、非目标和不可破坏的边界 |
| What：需求 | 设计与验收模板 | 具体场景、领域事实和待澄清问题 |
| How：架构 | 分层职责、关联模式和可运行示例 | 上下文/容器图、对象图、模块路径、生命周期和事务决策 |
| 知识与操作 | 框架术语、兼容性与集成指南 | 业务术语、启动/数据库/调试手册及环境前提 |
| Rules：规则与证据 | 反模式和接入检查清单 | API/安全/审计/多语言要求、精确检查命令、结果及评审人 |

不要因为 Demo 使用会计案例，就把会计业务规则复制到接入项目。项目需求和设计决策属于接入仓库，不属于本库。
如果项目已有工件审批流程，应通过该流程维护设计，不建立另一套竞争性的文档。

## 2. 建立明确的 AI 阅读入口

1. 选择[消费者 AGENTS 模板](../templates/consumer-AGENTS.zh-CN.md)的语言。
2. 合并到接入仓库的 `AGENTS.md`，不要覆盖已有指令。
3. 填完占位符，并将模板及双语对应链接调整为目标仓库内有效的链接。
4. 指向项目填写后的[关联矩阵](../templates/association-matrix.zh-CN.md)、[上下文角色](../templates/context-roles.zh-CN.md)和[接入检查清单](../templates/adoption-checklist.zh-CN.md)。
5. 其他 Agent 若需要 `CLAUDE.md`，让它简短引用同一份指令并确认 Agent 实际读取，不维护第二套架构政策。

本仓库根目录的 `AGENTS.md` 用于开发 Smart Domain 本身。下载依赖或上游存在 `AGENTS.md`，**不会**自动把指令传递给接入项目的 AI。

分别记录组件版本和文档 tag/commit。链接应固定到确实包含所选文档的 tag 或 commit，不使用变化中的 `main`。
发布后新增的模板并不会自动出现在旧版本 tag 中。离线快照应保留上游地址、修订号和获取日期；升级需要评审，不要静默修改上游契约。

模式契约是规范依据；指南、模板与 Demo 用于解释。接入项目的已批准需求/ADR 补充项目选择。
发生冲突时暂停受影响变更并请求明确决策；有意偏离时记录差异，不宣称完全遵循模式。

## 3. 添加 Starter 前先解决兼容性

修改宿主依赖前先阅读[兼容性矩阵](./compatibility.zh-CN.md)。可以先接入 Core，在保留现有 HTTP 框架的同时建立领域模型。
官方 API/MyBatis Starter 路径需要符合支持基线，或提供明确的集成验证证据。
不要静默替换宿主的 Spring Boot 版本、HTTP 栈、数据库或构建系统。

Java library 领域模块的最小 Gradle 依赖：

```gradle
plugins {
    id 'java-library'
}

repositories {
    mavenCentral()
}

dependencies {
    api platform('io.github.jayclock:smart-domain-bom:0.3.0')
    api 'io.github.jayclock:smart-domain-core'
}
```

使用宿主的 Java 17 toolchain 配置与测试设置。扩大集成前先验证依赖解析和最小消费者测试。
这段配置不会配置 HTTP 或数据库。

## 4. 映射分层，而不是建立 Service 流水线

下表定义职责，不强制目录名称或独立部署。应映射到现有仓库；需要强化依赖检查时再拆成独立构建模块。

| 层 | 负责 | 不负责 |
| --- | --- | --- |
| `bootstrap` | 启动、配置、适配器/角色解析器装配 | 业务决策或运行时业务门面 |
| `api` | 输入转换、根导航、角色切换、HAL 与领域错误映射 | 直接调用 Mapper 或业务策略 |
| `domain` | 实体、Description、根关联、拥有者定义的关联、角色及不变量 | 依赖 HTTP、Spring、Jackson 或 MyBatis |
| `infrastructure/persistence` | 加载、hydration、批处理、映射、写入与并发机制 | 准入规则或业务状态转换 |
| `infrastructure/security` | 可信参与者认证与身份集成 | 替代领域角色能力 |
| `infrastructure/transaction` | 原子执行与回滚 | 通过仓储编排业务的 Service |

编译期依赖：

```text
api --------------------> domain
infrastructure ---------> domain
bootstrap --------------> api + domain + infrastructure
```

运行时业务调用：

```text
HTTP resource -> root association -> entity / context role
              -> owner-private wide association -> adapter
```

不要求建立 `application` 包。已有边界可以提供技术性事务包装，但不能拥有领域决策。
把业务 Service 改名为 Store、Handler 或 Manager 不会纠正行为归属；反过来，技术辅助对象也不会仅因名称含 Service 就违规。
根关联可以暴露创建操作；窄/宽接口规则防止调用者绕过拥有者修改实体拥有的关联。

## 5. 编码前设计一个真实纵向场景

使用项目第一个已批准场景填写模板，不虚构包罗万象的业务模型：

- 根关联与连通对象图，说明为什么选择 `Ref`、`HasOne`、`HasMany` 或可选导航；
- 关联矩阵，以及拥有者/字段/宽接口/适配器/API rel 的精确对应；
- 行为归属、不变量、具体验收数据、失败路径和原子性要求；
- 参与者/上下文/角色能力及拒绝规则，或角色不适用的理由；
- 协议兼容、分页/错误、幂等、重试和并发预期；
- 业务术语，包括约定的中英文名称，以及可衡量的非功能风险。

需要解释外部参与者、存储或部署边界时补充上下文/容器图。它补充关联矩阵，不能替代行为归属设计。
未知基础设施与质量阈值保留为待决策项，不虚构默认值。

随后按以下顺序实现：

1. 使用关联 fake 编写领域行为测试，不启动 HTTP 或数据库。
2. 在实体/上下文角色中实现行为，保持拥有者的可变关联私有。
3. 在相同契约后实现生产适配器，运行共享的可观察关联契约用例。
4. 用技术性事务边界包住**整个领域操作**，在真实存储上验证回滚和并发不变量。多次写入的操作不能只为每次 Mapper 调用单独开启事务。
5. 用 API 根、链接与操作 affordance 投影同一对象图，保持已有契约兼容。
6. 在接入检查清单中记录测试、命令、结果、剩余风险和评审。

fake 可以证明领域决策，不能证明 SQL 隔离或真实认证。跨数据库、文件系统或远程服务的操作需要显式一致性/失败设计；不能把本地数据库事务描述为所有外部副作用都原子化。

## 6. 阅读现有纵向示例

不要为本指南另建一套断开的 Demo。沿着现有会计实现阅读：

| 关注点 | 现有参考 |
| --- | --- |
| 业务流程与拥有的关联 | [Customer](../demo/src/main/java/reengineering/ddd/demo/accounting/model/Customer.java) |
| 上下文相关行为 | [Bookkeeper](../demo/src/main/java/reengineering/ddd/demo/accounting/model/Bookkeeper.java) |
| 内存生命周期 | [InMemoryCustomers](../demo/src/main/java/reengineering/ddd/demo/accounting/memory/InMemoryCustomers.java) |
| 引用生命周期 | [AccountTransactions](../demo/src/main/java/reengineering/ddd/demo/accounting/mybatis/AccountTransactions.java) |
| HTTP 投影 | [AccountingApi](../demo/src/main/java/reengineering/ddd/demo/accounting/api/AccountingApi.java) |
| 领域与 API 证据 | [AccountingDemoTest](../demo/src/test/java/reengineering/ddd/demo/accounting/AccountingDemoTest.java)、[AccountingApiTest](../demo/src/test/java/reengineering/ddd/demo/accounting/AccountingApiTest.java) |

同时阅读[反模式](./anti-patterns.zh-CN.md)。Demo 的映射/Starter 测试不能证明接入项目的生产数据库、事务隔离或安全性。
Demo fixture 的身份也不是生产参与者解析方案。

## 7. 让合规性可以观察

以下已有命令在 **Smart Domain 源码检出目录**运行，用于验证库/Demo 和已发布组件的消费者示例：

```bash
./gradlew smartDomainCheck
./gradlew build publishToMavenLocal
./gradlew -p samples/consumer test
./gradlew -p samples/api-consumer test
```

`smartDomainCheck` 检查标准会计 Demo，不会随 Maven 依赖自动安装为接入项目的检查器。
消费者示例解析本地发布物，不能证明任意接入应用合规，也不能证明组件已经发布到 Maven Central。

接入项目必须提供自己的精确命令和证据：

- 自动依赖/API 表面检查：依赖方向及可变关联是否公开；
- 使用 fake 的领域场景，以及生产适配器执行的同一份关联契约用例；
- HTTP 业务场景验收、导航、错误和既有端点兼容性；
- 在支持团队的技术测试（Q1）和业务验收测试（Q2）之外，按风险安排可用性（Q3）以及安全/并发/性能（Q4）评价；
- 人工行为归属评审：名称或 import 扫描不能识别全部业务策略。

勾选模板不等于执行了测试。只有在[接入检查清单](../templates/adoption-checklist.zh-CN.md)中记录实际结果、明确的不适用理由及已评审风险后，才能宣布完成。
