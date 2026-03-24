package reengineering.ddd.demo.library.model;

import io.github.jayclock.smartdomain.core.Entity;
import io.github.jayclock.smartdomain.core.HasMany;
import reengineering.ddd.demo.library.description.BookDescription;
import reengineering.ddd.demo.library.description.ShelfDescription;

public class Shelf implements Entity<String, ShelfDescription> {
  private String identity;
  private ShelfDescription description;
  private Books books;

  public Shelf(String identity, ShelfDescription description, Books books) {
    this.identity = identity;
    this.description = description;
    this.books = books;
  }

  @Override
  public String getIdentity() {
    return identity;
  }

  @Override
  public ShelfDescription getDescription() {
    return description;
  }

  public HasMany<String, Book> books() {
    return books;
  }

  public Book add(BookDescription description) {
    return books.add(description);
  }

  public interface Books extends HasMany<String, Book> {
    Book add(BookDescription description);
  }
}
