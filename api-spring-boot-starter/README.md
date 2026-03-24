# Smart Domain API Spring Boot Starter

`smart-domain-api-spring-boot-starter` auto-configures:

- HAL and HAL-FORMS ObjectMapper support
- `HalFormsConfiguration` aggregation
- JSON Schema-backed HAL-FORMS options when `smart-domain.api.schema-scan-packages` is set
- Jersey registration of `VendorMediaTypeInterceptor`

Typical usage:

```yaml
smart-domain:
  api:
    schema-scan-packages:
      - com.example.library.api
```
