# smart-domain-api-jersey

[English](./README.md) | 简体中文

为 Smart Domain API 提供 Jersey 专用支持。

状态：高级底层模块，不是首选公共入口。

当前提供：

- `VendorMediaTypeInterceptor`
- Smart Domain Spring Boot API 的 Jersey `ResourceConfig` 自动配置

需要 Jersey 集成但不希望使用完整 Spring Boot Starter 时，可以直接使用本模块。如果已经使用 Spring Boot，优先选择 `smart-domain-api-spring-boot-starter`。
