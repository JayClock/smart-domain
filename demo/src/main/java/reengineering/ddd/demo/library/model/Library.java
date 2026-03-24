package reengineering.ddd.demo.library.model;

import io.github.jayclock.smartdomain.core.Entity;
import io.github.jayclock.smartdomain.core.HasMany;
import reengineering.ddd.demo.library.description.LibraryDescription;
import reengineering.ddd.demo.library.description.ShelfDescription;

public class Library implements Entity<String, LibraryDescription> {
  private String identity;
  private LibraryDescription description;
  private Shelves shelves;

  public Library(String identity, LibraryDescription description, Shelves shelves) {
    this.identity = identity;
    this.description = description;
    this.shelves = shelves;
  }

  @Override
  public String getIdentity() {
    return identity;
  }

  @Override
  public LibraryDescription getDescription() {
    return description;
  }

  public HasMany<String, Shelf> shelves() {
    return shelves;
  }

  public Shelf add(ShelfDescription description) {
    return shelves.add(description);
  }

  public interface Shelves extends HasMany<String, Shelf> {
    Shelf add(ShelfDescription description);
  }
}
