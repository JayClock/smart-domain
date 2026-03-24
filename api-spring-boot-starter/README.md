# Smart Domain API Spring Boot Starter

`smart-domain-api-spring-boot-starter` auto-configures:

- HAL and HAL-FORMS ObjectMapper support
- `HalFormsConfiguration` aggregation
- JSON Schema-backed HAL-FORMS options when `smart-domain.api.schema-scan-packages` is set
- Smart Domain API Jersey integration through `smart-domain-api-jersey`

This is one of the primary public entrypoints for Smart Domain API adoption.

Typical usage:

```yaml
smart-domain:
  api:
    schema-scan-packages:
      - com.example.library.api
```

For a complete external consumer example, see [`samples/api-consumer`](../samples/api-consumer/README.md).

Module relationship:

- `smart-domain-api-hateoas` provides the core API annotations and HAL-FORMS helpers
- `smart-domain-api-jersey` adds Jersey integration
- `smart-domain-api-spring-boot-starter` auto-configures both for Spring Boot consumers
