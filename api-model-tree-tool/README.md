# Smart Domain API Model Tree Tool

This module provides an external-facing utility for turning Java HATEOAS API model classes into a
recursive JSON navigation tree for AI agents.

It is intended for API-layer model classes such as:

- `RootModel extends RepresentationModel<RootModel>`
- `UserModel extends RepresentationModel<UserModel>`
- `ProjectModel extends RepresentationModel<ProjectModel>`

The public entrypoint is a Java model class such as `UserModel.class`. The tool derives the
matching `.java` source from the compiled class, then extracts link relations declared through:

- `withRel("...")`
- `withSelfRel()`
- `withName("...")`
- `Link.of(..., "rel")`

Then it resolves recursively involved models from that source file's actual Java model references:

- imported `*Model` classes
- same-package `*Model` type references
- `new XxxModel(...)`, `XxxModel.someFactory(...)`, record fields, method signatures, and generic
  type references

The tool matches each link relation only inside that actually referenced model set. It does not
scan the whole directory from relation names alone.

For each link it also resolves the RESTful API template from:

- `Link.of(...)`
- `ApiTemplates.*(...)`
- `UriBuilder.path(ApiClass.class, "methodName")`
- JAX-RS `@Path` annotations on resource classes and methods

## Usage

```java
ApiModelNode tree = SmartDomainTools.apiModelTree(UserModel.class);
String json = SmartDomainTools.apiModelTreeAsJson(UserModel.class);
String jsonWithCycle =
    SmartDomainTools.apiModelTreeAsJson(
        UserModel.class,
        new ApiModelTreeOptions(true));
```

The public entrypoint is the model class itself. The tool derives the matching `.java` source file
from the compiled class using the standard Gradle source layout:

- `build/classes/java/main` -> `src/main/java`
- `build/classes/java/test` -> `src/test/java`

## Output shape

```json
{
  "rel": "self",
  "api": "/users/{id}",
  "links": [
    {
      "rel": "update-user",
      "api": "/users/{id}",
      "links": []
    },
    {
      "rel": "accounts",
      "api": "/users/{id}/accounts",
      "links": []
    },
    {
      "rel": "default-project",
      "api": "/projects/{projectId}",
      "links": [
        {
          "rel": "agents",
          "api": "/projects/{projectId}/agents",
          "links": []
        }
      ]
    }
  ]
}
```

If a source link declares one or more `withName(...)` affordance names, each name is emitted as
its own link node with the same `api`.

When `includeCycleMarkers` or `--include-cycle` is enabled, a truncated loop edge is emitted as:

```json
{
  "rel": "project",
  "api": "/projects/{projectId}",
  "cycle": true,
  "links": []
}
```
