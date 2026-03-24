# Smart Domain Naming Conventions

Smart Domain keeps a 1:1 naming chain from domain model to persistence adapter.

## Recommended Chain

`Library.shelves -> LibraryShelves -> Mapper -> XML -> Starter`

## Rules

- Association field names stay in the domain language: `Library.shelves`.
- The adapter class mirrors the owner and field: `LibraryShelves`.
- The MyBatis mapper follows the adapter name: `LibraryShelvesMapper`.
- The XML file follows the mapper name: `LibraryShelvesMapper.xml`.
- `@EnableSmartDomainMybatis` points `associationBasePackages` at the package where
  `LibraryShelves` lives.

## Example

| Layer | Example |
| --- | --- |
| Domain entity field | `Library.shelves` |
| Association adapter | `LibraryShelves` |
| Mapper interface | `LibraryShelvesMapper` |
| Mapper XML | `LibraryShelvesMapper.xml` |
| Starter scan root | `com.example.library.mybatis` |
