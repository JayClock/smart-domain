# Smart Domain 0.3.0 发布就绪状态

[English](./release-readiness.md) | 简体中文

## 当前本地发布状态

截至 2026-09-03，仓库已经生成并验证以下本地发布组件：

- `io.github.jayclock:smart-domain-core:0.3.0`
- `io.github.jayclock:smart-domain-api-hateoas:0.3.0`
- `io.github.jayclock:smart-domain-api-jersey:0.3.0`
- `io.github.jayclock:smart-domain-api-spring-boot-starter:0.3.0`
- `io.github.jayclock:smart-domain-persistence:0.3.0`
- `io.github.jayclock:smart-domain-mybatis:0.3.0`
- `io.github.jayclock:smart-domain-mybatis-spring-boot-starter:0.3.0`
- `io.github.jayclock:smart-domain-bom:0.3.0`

## 公共产品入口

`0.3.x` 推荐从以下组件开始：

- `io.github.jayclock:smart-domain-bom`
- `io.github.jayclock:smart-domain-core`
- `io.github.jayclock:smart-domain-api-spring-boot-starter`
- `io.github.jayclock:smart-domain-mybatis-spring-boot-starter`

以下组件继续用于高级组合，但不是首选入口：

- `smart-domain-api-hateoas`
- `smart-domain-api-jersey`
- `smart-domain-persistence`
- `smart-domain-mybatis`

## 已验证命令

从产品根目录发布到 `mavenLocal`：

```bash
cd smart-domain
./gradlew \
  :core:publishToMavenLocal \
  :api-hateoas:publishToMavenLocal \
  :api-jersey:publishToMavenLocal \
  :api-spring-boot-starter:publishToMavenLocal \
  :persistence:publishToMavenLocal \
  :mybatis:publishToMavenLocal \
  :mybatis-spring-boot-starter:publishToMavenLocal \
  :bom:publishToMavenLocal
```

外部使用示例验证：

```bash
./gradlew -p samples/consumer test
./gradlew -p samples/api-consumer test
```

## 0.3.x 兼容性说明

- 支持基线为 Java 17、Spring Boot 3.5.x 和 MyBatis Spring Boot Starter 3.0.x；
- 发布到 Central 前，外部示例使用 `mavenLocal` 中的组件验证；
- Maven Central 发布由仓库发布工作流自动完成；
- 目前不包含 JPA 集成、代码生成器或多数据库方言抽象；
- 原 API model-tree 工具及其演示端点不属于 0.3.x 产品范围。
