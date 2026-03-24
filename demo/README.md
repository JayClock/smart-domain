# Smart Domain Basic Demo

This module is the smallest runnable example of the Smart Domain pattern extracted from Team AI.

It demonstrates one-to-one correspondence across three layers:

| Model Layer | Wide Interface | Persistence Adapter | Storage Record |
| --- | --- | --- | --- |
| `Library.shelves` | `Library.Shelves` | `LibraryShelves` | `ShelfRecord` |
| `Shelf.books` | `Shelf.Books` | `ShelfBooks` | `BookRecord` |
| `Libraries` | `Libraries` | `InMemoryLibraries` | `LibraryRecord` |

It also includes a MyBatis-oriented template:

| Model Layer | MyBatis Association Adapter | Mapper | XML Snippet |
| --- | --- | --- | --- |
| `Library.shelves` | `mybatis.LibraryShelves` | `LibraryShelvesMapper` | `library-demo/LibraryShelvesMapper.xml` |
| `Shelf.books` | `mybatis.ShelfBooks` | `ShelfBooksMapper` | `library-demo/ShelfBooksMapper.xml` |
| `Libraries` | `mybatis.MybatisLibraries` | `LibrariesMapper` | `library-demo/LibrariesMapper.xml` |

And it includes a starter-oriented template for bootstrapping the runtime:

| Runtime Layer | Demo Type | Declares |
| --- | --- | --- |
| `mybatis/config/LibraryDemoSmartDomainMybatisConfiguration` | `@EnableSmartDomainMybatis` | `associationBasePackages = "reengineering.ddd.demo.library.mybatis"` |
| `mybatis/config/LibraryDemoSmartDomainMybatisConfiguration` | `leafEntityTypes` | `Book.class` |

## Why this demo exists

- Keep the example independent from Team AI business concepts
- Show how `HasMany` becomes a first-class domain object
- Show that every model association can have a matching persistence adapter
- Provide a copyable template for future projects

## Structure

```text
demo/
├── description/
│   ├── LibraryDescription
│   ├── ShelfDescription
│   └── BookDescription
├── model/
│   ├── Library
│   ├── Shelf
│   ├── Book
│   └── Libraries
├── memory/
    ├── InMemoryLibraries
    ├── LibraryShelves
    ├── ShelfBooks
    └── MemoryAssociation
└── mybatis/
    ├── MybatisLibraries
    ├── LibraryShelves
    ├── ShelfBooks
    ├── config/
    ├── mappers/
    └── resources/library-demo/*.xml
```

## The correspondence rule

The pattern used in this demo is:

1. The entity owns a field like `private Shelves shelves;`
2. The entity exposes a narrow interface like `HasMany<String, Shelf> shelves()`
3. The entity defines a wide interface like `interface Shelves extends HasMany<String, Shelf> { ... }`
4. The persistence adapter implements that wide interface with a matching class name like `LibraryShelves`
5. The starter config points to the adapter package and leaf entities with `@EnableSmartDomainMybatis`

That naming rule is what keeps the model layer and persistence layer aligned.

## MyBatis correspondence example

The MyBatis template in this module keeps the same naming and ownership rule:

- Model field: `Library.shelves`
- Wide interface: `Library.Shelves`
- Adapter class: `reengineering.ddd.demo.library.mybatis.LibraryShelves`
- Mapper interface: `LibraryShelvesMapper`
- Mapper XML association target: `javaType="reengineering.ddd.demo.library.mybatis.LibraryShelves"`

The same rule applies to `Shelf.books -> ShelfBooks`.

This is intentionally parallel to the Team AI production pattern, but with a much smaller model.

## Starter correspondence example

The starter demo adds the runtime layer without introducing Team AI business packages:

- Model ownership: `Library.shelves`, `Shelf.books`
- Association scan root: `reengineering.ddd.demo.library.mybatis`
- Leaf entity registration: `Book.class`
- Starter descriptor: `mybatis.config.LibraryDemoSmartDomainMybatisConfiguration`

That means a new project only needs to keep three things aligned:

1. Model fields and wide interfaces
2. Association adapter classes and mapper files
3. One `@EnableSmartDomainMybatis(...)` descriptor that points at the adapter package and leaf entities
