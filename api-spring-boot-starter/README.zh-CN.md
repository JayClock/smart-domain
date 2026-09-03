# Smart Domain API Spring Boot Starter

[English](./README.md) | 简体中文

`smart-domain-api-spring-boot-starter` 自动配置：

- HAL 和 HAL-FORMS 的 ObjectMapper 支持；
- `HalFormsConfiguration` 聚合；
- 设置 `smart-domain.api.schema-scan-packages` 后，提供基于 JSON Schema 的 HAL-FORMS 选项；
- 通过 `smart-domain-api-jersey` 集成 Jersey。

这是 Smart Domain API 的主要公共入口之一。

典型配置：

```yaml
smart-domain:
  api:
    schema-scan-packages:
      - com.example.accounting.api
```

完整外部使用示例见 [`samples/api-consumer`](../samples/api-consumer/README.zh-CN.md)。

模块关系：

- `smart-domain-api-hateoas` 提供核心 API 注解和 HAL-FORMS 工具；
- `smart-domain-api-jersey` 提供 Jersey 集成；
- `smart-domain-api-spring-boot-starter` 为 Spring Boot 使用者自动配置前两者。
