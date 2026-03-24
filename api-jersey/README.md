# smart-domain-api-jersey

Jersey-specific support for Smart Domain APIs.

Status: advanced low-level module, not the primary public entrypoint.

This module currently provides:

- `VendorMediaTypeInterceptor`
- Jersey `ResourceConfig` auto-configuration for Smart Domain API Spring Boot apps

Use this module when you want Jersey integration without taking the full Spring Boot starter.

If you are already on Spring Boot, prefer `smart-domain-api-spring-boot-starter`.
