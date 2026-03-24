# Smart Domain API Quick Start

This guide shows the minimum path for exposing a Smart Domain backed REST API with Jersey, HAL and
HAL-FORMS.

## 1. Add Dependencies

Use the BOM and starter:

```gradle
dependencies {
    implementation platform("io.github.jayclock.smartdomain:smart-domain-bom:${smartDomainVersion}")
    implementation 'io.github.jayclock.smartdomain:smart-domain-api-spring-boot-starter'
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
      - com.example.library.api
```

## 3. Register Jersey Resources

```java
@Configuration
public class LibraryJerseyConfiguration extends ResourceConfig {
  public LibraryJerseyConfiguration() {
    register(BooksApi.class);
  }
}
```

## 4. Build A Resource

```java
@Component
@Path("books")
@Produces(MediaType.APPLICATION_JSON)
public class BooksApi {
  @GET
  @VendorMediaType(BookMediaTypes.BOOK_COLLECTION)
  public CollectionModel<BookModel> findAll(@Context UriInfo uriInfo) {
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
public class BookOptionsCustomizer implements HalFormsOptionsCustomizer {
  @Override
  public HalFormsConfiguration customize(HalFormsConfiguration config) {
    return config.withOptions(CreateBookRequest.class, "genre", metadata -> ...);
  }
}
```

Use `@WithJsonSchema` on input fields that should expose JSON Schema in HAL-FORMS:

```java
public record CreateBookRequest(
    String title,
    @WithJsonSchema(BookMetadata.class) BookMetadata metadata) {}
```

## 6. Run The Published Sample

See [API Consumer Sample](./samples/api-consumer/README.md) for a complete working example that
consumes published artifacts from `mavenLocal`.
