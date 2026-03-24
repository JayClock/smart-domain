package reengineering.ddd.demo.library.memory;

import java.util.List;
import reengineering.ddd.demo.library.description.ShelfDescription;
import reengineering.ddd.demo.library.model.Library;
import reengineering.ddd.demo.library.model.Shelf;

public class LibraryShelves extends MemoryAssociation<String, Shelf> implements Library.Shelves {
  private final InMemoryLibraries store;
  private final String libraryId;

  public LibraryShelves(InMemoryLibraries store, String libraryId) {
    this.store = store;
    this.libraryId = libraryId;
  }

  @Override
  public Shelf add(ShelfDescription description) {
    return store.createShelf(libraryId, description);
  }

  @Override
  protected List<Shelf> snapshot() {
    return store.shelvesOf(libraryId);
  }
}
