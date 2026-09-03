# Smart Domain 独立仓库就绪检查

[English](./repository-split-readiness.md) | 简体中文

本清单记录将 `smart-domain/` 迁移为独立仓库，同时保持公共 Maven 坐标不变所需的工作。

## 已独立的部分

- 产品可以直接从 `smart-domain/` 根目录构建；
- 产品模块使用 `io.github.jayclock` 坐标发布；
- API Starter、Jersey 集成和外部示例均位于本仓库；
- 发布元数据由 `smartDomain*` Gradle 属性管理。

## 独立前剩余工作

1. 确认最终仓库 URL，并更新 `smartDomainProjectUrl` 和 SCM 属性；
2. 增加 `LICENSE`、Issue 模板、发布工作流和 Central 发布 Secret；
3. 移动或重新创建仍重复保存在 `docs/smart-domain/` 下的产品文档；
4. 增加直接运行 `./gradlew build` 以及通过已发布快照验证示例的 CI；
5. 从最终仓库命名空间发布第一个外部快照，并验证仓库外部使用。

## 推荐迁移顺序

1. 冻结公共坐标和模块名称；
2. 创建独立仓库，并以当前 `smart-domain/` 目录作为根目录；
3. 迁移 CI、发布 Secret 和签名配置；
4. 发布新快照；
5. 让下游应用改为使用已发布组件，而不是兄弟项目引用。
