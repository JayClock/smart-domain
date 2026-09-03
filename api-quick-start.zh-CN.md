# Smart Domain API 快速开始

[English](./api-quick-start.md) | 简体中文

本指南介绍使用 Jersey、HAL 和 HAL-FORMS 暴露 Smart Domain REST API 的最短路径。示例使用会计 Demo 的领域语言。

## 1. 添加依赖

使用 BOM 和 Starter：

```gradle
dependencies {
    implementation platform("io.github.jayclock:smart-domain-bom:${smartDomainVersion}")
    implementation 'io.github.jayclock:smart-domain-api-spring-boot-starter'
}
```

## 2. 配置 Spring Boot

```yaml
spring:
  jersey:
    application-path: /api
    type: filter

smart-domain:
  api:
    schema-scan-packages:
      - com.example.accounting.api
```

## 3. 注册 Jersey 资源

```java
@Configuration
public class AccountingJerseyConfiguration extends ResourceConfig {
  public AccountingJerseyConfiguration() {
    register(CustomersApi.class);
  }
}
```

## 4. 创建资源

```java
@Component
@Path("customers/{customerId}/source-evidences/sales-settlements")
@Produces(MediaType.APPLICATION_JSON)
public class SalesSettlementsApi {
  @GET
  @VendorMediaType(AccountingMediaTypes.SALES_SETTLEMENT_COLLECTION)
  public CollectionModel<SourceEvidenceModel> findAll(@Context UriInfo uriInfo) {
    Link self = Link.of(uriInfo.getAbsolutePath().toString()).withSelfRel();
    return CollectionModel.of(List.of(), self);
  }
}
```

`@VendorMediaType` 来自 `smart-domain-api-hateoas`。Spring Boot Starter 会配置 Jersey 拦截器和 HAL ObjectMapper。

## 5. 扩展 HAL-FORMS

使用 `HalFormsOptionsCustomizer` 提供内联或远程选项：

```java
@Component
public class AccountingOptionsCustomizer implements HalFormsOptionsCustomizer {
  @Override
  public HalFormsConfiguration customize(HalFormsConfiguration config) {
    return config.withOptions(
        CreateSalesSettlementRequest.class,
        "accountId",
        metadata -> ...);
  }
}
```

需要在 HAL-FORMS 中暴露 JSON Schema 的输入字段可以使用 `@WithJsonSchema`：

```java
public record CreateSalesSettlementRequest(
    String orderId,
    String accountId,
    @WithJsonSchema(SettlementBreakdown.class) SettlementBreakdown breakdown) {}
```

## 6. 运行已发布包的示例

完整示例见 [API Consumer Sample](./samples/api-consumer/README.zh-CN.md)。它从 `mavenLocal` 使用已发布的 Smart Domain 组件。
