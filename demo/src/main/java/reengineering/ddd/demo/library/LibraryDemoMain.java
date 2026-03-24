package reengineering.ddd.demo.library;

import reengineering.ddd.demo.library.description.BookDescription;
import reengineering.ddd.demo.library.description.LibraryDescription;
import reengineering.ddd.demo.library.description.ShelfDescription;
import reengineering.ddd.demo.library.memory.InMemoryLibraries;

public class LibraryDemoMain {
  public static void main(String[] args) {
    InMemoryLibraries libraries = new InMemoryLibraries();

    var library = libraries.create(new LibraryDescription("City Library"));
    var shelf = library.add(new ShelfDescription("Domain-Driven Design"));
    shelf.add(new BookDescription("Smart Domain"));
    shelf.add(new BookDescription("Association Objects"));

    System.out.println("Library: " + library.getDescription().name());
    System.out.println("Shelf adapter: " + library.shelves().getClass().getSimpleName());
    System.out.println("Book adapter: " + shelf.books().getClass().getSimpleName());
    System.out.println("Books count: " + shelf.books().findAll().size());
  }
}
