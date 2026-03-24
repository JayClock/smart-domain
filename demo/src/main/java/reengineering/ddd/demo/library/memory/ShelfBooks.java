package reengineering.ddd.demo.library.memory;

import java.util.List;
import reengineering.ddd.demo.library.description.BookDescription;
import reengineering.ddd.demo.library.model.Book;
import reengineering.ddd.demo.library.model.Shelf;

public class ShelfBooks extends MemoryAssociation<String, Book> implements Shelf.Books {
  private final InMemoryLibraries store;
  private final String shelfId;

  public ShelfBooks(InMemoryLibraries store, String shelfId) {
    this.store = store;
    this.shelfId = shelfId;
  }

  @Override
  public Book add(BookDescription description) {
    return store.createBook(shelfId, description);
  }

  @Override
  protected List<Book> snapshot() {
    return store.booksOf(shelfId);
  }
}
