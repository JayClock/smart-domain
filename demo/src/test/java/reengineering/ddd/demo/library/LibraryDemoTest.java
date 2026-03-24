package reengineering.ddd.demo.library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import reengineering.ddd.demo.library.description.BookDescription;
import reengineering.ddd.demo.library.description.LibraryDescription;
import reengineering.ddd.demo.library.description.ShelfDescription;
import reengineering.ddd.demo.library.memory.InMemoryLibraries;
import reengineering.ddd.demo.library.memory.LibraryShelves;
import reengineering.ddd.demo.library.memory.ShelfBooks;

class LibraryDemoTest {

  @Test
  void should_show_one_to_one_correspondence_between_model_and_adapter() {
    InMemoryLibraries libraries = new InMemoryLibraries();

    var library = libraries.create(new LibraryDescription("City Library"));
    var shelf = library.add(new ShelfDescription("Architecture"));
    var book = shelf.add(new BookDescription("Smart Domain"));

    assertInstanceOf(LibraryShelves.class, library.shelves());
    assertInstanceOf(ShelfBooks.class, shelf.books());
    assertEquals("Smart Domain", book.getDescription().title());
  }

  @Test
  void should_rehydrate_the_same_model_shape_from_repository() {
    InMemoryLibraries libraries = new InMemoryLibraries();

    var createdLibrary = libraries.create(new LibraryDescription("Community Library"));
    var createdShelf = createdLibrary.add(new ShelfDescription("DDD"));
    createdShelf.add(new BookDescription("Modeling with Associations"));
    createdShelf.add(new BookDescription("Hydration by Design"));

    var reloadedLibrary = libraries.findByIdentity(createdLibrary.getIdentity()).orElseThrow();

    assertEquals(createdLibrary.getDescription().name(), reloadedLibrary.getDescription().name());
    assertEquals(1, reloadedLibrary.shelves().findAll().size());

    var reloadedShelf =
        reloadedLibrary.shelves().findByIdentity(createdShelf.getIdentity()).orElseThrow();
    assertEquals("DDD", reloadedShelf.getDescription().name());
    assertEquals(2, reloadedShelf.books().findAll().size());
    assertTrue(reloadedShelf.books().findByIdentity("1").isPresent());
  }
}
