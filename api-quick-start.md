# Smart Domain API Quick Start

This guide shows the minimum path for exposing a Smart Domain backed REST API with Jersey, HAL and
HAL-FORMS.

The sample language here is the accounting case used by `demo/accounting`.

## 1. Add Dependencies

Use the BOM and starter:

```gradle
dependencies {
    implementation platform("io.github.jayclock:smart-domain-bom:${smartDomainVersion}")
    implementation 'io.github.jayclock:smart-domain-api-spring-boot-starter'
}
```

## 2. Configure Spring Boot

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

## 3. Register Jersey Resources

```java
@Configuration
public class AccountingJerseyConfiguration extends ResourceConfig {
  public AccountingJerseyConfiguration() {
    register(CustomersApi.class);
  }
}
```

## 4. Build A Resource

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

`@VendorMediaType` comes from `smart-domain-api-hateoas`. The Spring Boot starter takes care of the
Jersey interceptor and HAL object mapper configuration.

## 5. Extend HAL-FORMS

Use `HalFormsOptionsCustomizer` for inline or remote options:

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

Use `@WithJsonSchema` on input fields that should expose JSON Schema in HAL-FORMS:

```java
public record CreateSalesSettlementRequest(
    String orderId,
    String accountId,
    @WithJsonSchema(SettlementBreakdown.class) SettlementBreakdown breakdown) {}
```

## 6. Run The Published Sample

See [API Consumer Sample](./samples/api-consumer/README.md) for a complete working example that
consumes published artifacts from `mavenLocal`.
