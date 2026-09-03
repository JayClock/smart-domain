# Smart Domain 会计 API 使用示例

[English](./README.md) | 简体中文

该示例从 `mavenLocal` 使用已发布的 Smart Domain API 组件，演示：

- `smart-domain-api-spring-boot-starter`
- Jersey 资源注册
- `@VendorMediaType`
- 账户选择的 HAL-FORMS 选项
- 结算明细请求的 JSON Schema 扫描

先在产品根目录发布本地组件，再运行示例测试：

```bash
cd smart-domain
./gradlew \
  :core:publishToMavenLocal \
  :api-hateoas:publishToMavenLocal \
  :api-jersey:publishToMavenLocal \
  :api-spring-boot-starter:publishToMavenLocal \
  :bom:publishToMavenLocal

./gradlew -p samples/api-consumer test
```
