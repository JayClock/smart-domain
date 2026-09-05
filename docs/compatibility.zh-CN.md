# 消费者兼容性

[English](./compatibility.md) | 简体中文

范围：`0.3.x` 产品线；以下源码/示例的具体版本对应 `0.3.0`。
发布验证背景见[发布就绪状态](./release-readiness.zh-CN.md)，迁移步骤见[接入指南](./adoption-guide.zh-CN.md)。

## 支持基线与可选集成

| 能力 | 基线 / 源码依据 | 接入项目的决策 |
| --- | --- | --- |
| `smart-domain-core` | Java 17；[core/build.gradle](../core/build.gradle) 没有生产依赖 | 可以先接入，不安装 HTTP 或持久化 Starter |
| 官方 Spring Boot Starter | Spring Boot 3.5.x；源码 BOM 与 API 示例使用 3.5.9 | 验证宿主解析后的依赖图和启动行为，而不只是 import |
| MyBatis 集成 | MyBatis Spring Boot Starter 3.0.x；Starter 使用 3.0.4 | 选择 MyBatis 时才引入；配置数据源、Mapper 和关联发现 |
| 官方 HTTP 集成 | Jersey、Spring HATEOAS、HAL/HAL-FORMS | 验证资源注册、媒体类型、序列化、错误和已有路由 |
| 其他 HTTP/存储适配器 | 扩展点仍是领域拥有者定义的契约 | 由接入项目实现和测试，不能自动视为官方 Starter 配置 |
| Spring Boot 4.x | 不在已说明的 0.3.x Starter 基线内 | 视为未验证，既不宣称已兼容，也不宣称不可能兼容 |
| JPA / 代码生成 / 多 SQL 方言 | 0.3.x 未提供相应集成或抽象 | 不从领域接口推断这些能力已经受支持 |

可执行的版本依据是[根构建](../build.gradle)、[API 消费者构建](../samples/api-consumer/build.gradle)、[MyBatis Starter 构建](../mybatis-spring-boot-starter/build.gradle)和[API Starter 构建](../api-spring-boot-starter/build.gradle)。
Starter 的传递依赖图不能等同于 Core 无生产依赖的运行时表面。

## 选择接入路径

- **受支持的 Starter 路径**：通过已批准的项目决策对齐支持基线，再按需采用 [MyBatis Starter](../mybatis-spring-boot-starter/README.zh-CN.md)和 [API 快速开始](../api-quick-start.zh-CN.md)。
- **Core 优先路径**：保留宿主技术栈，先建立领域关联/角色。保留 MVC 不违反领域模式，但 Core 不会自动配置 MVC，也不提供 Jersey 集成。
- **新基线适配路径**：宿主不能变更但需要完整 Starter 时，隔离兼容性工作，执行测试并记录受支持组合。强制覆盖版本或一次编译成功不能作为充分证据。

不要静默降级 Spring Boot、将 MVC 换为 Jersey、添加数据库或替换宿主构建系统。
在 ADR 中记录背景、备选方案、决定、后果、负责人及回滚方式。
只选择需要的组件；安装全部组件不等于架构合规。

## 版本与依赖来源策略

- 用 `smart-domain-bom` 对齐组件，固定具体发布版本，不使用动态版本。
- 发布版本使用 Maven Central，并检查所选组件确实能从该仓库解析。Git tag、CI 结果或 `publishToMavenLocal` 不能证明 Central 可用性。
- 本地发布只用于明确标识的源码/消费者验证。本仓库示例使用 `mavenLocal`，不要无意把该优先级复制到生产构建中。
- 在接入记录中保存库版本、文档 tag/commit、宿主 JDK/Boot/MyBatis 版本和依赖仓库。文档修订必须确实包含所用文件。
- 优先使用稳定入口。需要高级 API 时记录理由；不要因为库内部测试使用了某个类型，就依赖内部启动实现。

## 必需的兼容性证据

在接入项目中宣称集成受支持前，应记录：

1. 依赖解析结果，没有意外框架降级或 Smart Domain 组件版本错配。
2. 所选 JDK 上的领域编译与行为测试。
3. HTTP 场景：应用启动、原有路由/健康检查、HAL/媒体类型、错误和参与者处理。
4. MyBatis 场景：发现/hydration、真实数据库读写、分页、事务回滚和并发不变量测试。Mock Mapper 或反射测试不能证明 SQL 行为。
5. 干净环境下可复现的构建/测试命令，以及未解决限制和回滚步骤。

未选择的集成应记录不适用及理由，不为其虚构通过结果。
