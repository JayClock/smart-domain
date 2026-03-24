# smart-domain-api-hateoas

Reusable HATEOAS and HAL-FORMS support for Smart Domain based APIs.

This module is intentionally framework-support oriented. It does not ship Team AI resource models or
business endpoints. Instead it provides:

- vendor media type annotations and helpers
- pagination for Smart Domain collections
- HAL-FORMS option abstractions
- JSON Schema integration for HAL-FORMS inputs

Jersey-specific response interception lives in `smart-domain-api-jersey`.

Choose this module when you need:

- `@VendorMediaType`
- Smart Domain pagination wrappers for HAL resources
- `HalFormsOptionsCustomizer`
- `@WithJsonSchema` and JSON Schema-backed HAL-FORMS fields

If you are on Spring Boot with Jersey, prefer `smart-domain-api-spring-boot-starter` and let it
pull this module in transitively.
