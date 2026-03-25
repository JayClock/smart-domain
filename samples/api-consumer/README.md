# Smart Domain Accounting API Consumer Sample

This sample consumes the published Smart Domain API artifacts from `mavenLocal`.

It demonstrates a minimal accounting-facing API consumer over published Smart Domain artifacts:

- `smart-domain-api-spring-boot-starter`
- Jersey resource registration
- `@VendorMediaType`
- HAL-FORMS options for account selection
- JSON Schema scanning for settlement breakdown payloads

Run from the product root after publishing snapshots:

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
