package reengineering.ddd.demo.library.memory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import reengineering.ddd.demo.library.description.BookDescription;
import reengineering.ddd.demo.library.description.LibraryDescription;
import reengineering.ddd.demo.library.description.ShelfDescription;
import reengineering.ddd.demo.library.model.Book;
import reengineering.ddd.demo.library.model.Libraries;
import reengineering.ddd.demo.library.model.Library;
import reengineering.ddd.demo.library.model.Shelf;

public class InMemoryLibraries implements Libraries {
  private final Map<String, LibraryRecord> libraries = new LinkedHashMap<>();
  private final Map<String, ShelfRecord> shelves = new LinkedHashMap<>();
  private final Map<String, BookRecord> books = new LinkedHashMap<>();

  private int nextLibraryId = 1;
  private int nextShelfId = 1;
  private int nextBookId = 1;

  @Override
  public Library create(LibraryDescription description) {
    String identity = String.valueOf(nextLibraryId++);
    libraries.put(identity, new LibraryRecord(identity, description));
    return rehydrateLibrary(identity);
  }

  @Override
  public Optional<Library> findByIdentity(String identity) {
    if (!libraries.containsKey(identity)) {
      return Optional.empty();
    }
    return Optional.of(rehydrateLibrary(identity));
  }

  Shelf createShelf(String libraryId, ShelfDescription description) {
    String identity = String.valueOf(nextShelfId++);
    shelves.put(identity, new ShelfRecord(identity, libraryId, description));
    return rehydrateShelf(identity);
  }

  Book createBook(String shelfId, BookDescription description) {
    String identity = String.valueOf(nextBookId++);
    books.put(identity, new BookRecord(identity, shelfId, description));
    return rehydrateBook(identity);
  }

  List<Shelf> shelvesOf(String libraryId) {
    return shelves.values().stream()
        .filter(record -> record.libraryId().equals(libraryId))
        .map(record -> rehydrateShelf(record.identity()))
        .toList();
  }

  List<Book> booksOf(String shelfId) {
    return books.values().stream()
        .filter(record -> record.shelfId().equals(shelfId))
        .map(record -> rehydrateBook(record.identity()))
        .toList();
  }

  Library rehydrateLibrary(String libraryId) {
    LibraryRecord record = libraries.get(libraryId);
    return new Library(
        record.identity(), record.description(), new LibraryShelves(this, libraryId));
  }

  Shelf rehydrateShelf(String shelfId) {
    ShelfRecord record = shelves.get(shelfId);
    return new Shelf(record.identity(), record.description(), new ShelfBooks(this, shelfId));
  }

  Book rehydrateBook(String bookId) {
    BookRecord record = books.get(bookId);
    return new Book(record.identity(), record.description());
  }

  private record LibraryRecord(String identity, LibraryDescription description) {}

  private record ShelfRecord(String identity, String libraryId, ShelfDescription description) {}

  private record BookRecord(String identity, String shelfId, BookDescription description) {}
}
