# smart-domain-api-hateoas

[English](./README.md) | 简体中文

为基于 Smart Domain 的 API 提供可复用的 HATEOAS 和 HAL-FORMS 支持。

状态：高级底层模块，不是首选公共入口。

该模块只提供框架支持，不包含应用专用资源模型或业务端点，具体包括：

- 厂商媒体类型注解和辅助工具；
- Smart Domain 集合分页；
- HAL-FORMS 选项抽象；
- HAL-FORMS 输入的 JSON Schema 集成。

Jersey 专用响应拦截位于 `smart-domain-api-jersey`。

需要以下能力时可以直接使用本模块：

- `@VendorMediaType`
- HAL 资源的 Smart Domain 分页包装
- `HalFormsOptionsCustomizer`
- `@WithJsonSchema` 和基于 JSON Schema 的 HAL-FORMS 字段

如果使用 Spring Boot 和 Jersey，优先依赖 `smart-domain-api-spring-boot-starter`，让它传递引入本模块。
