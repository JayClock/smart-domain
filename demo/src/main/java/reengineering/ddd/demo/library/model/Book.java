package reengineering.ddd.demo.library.model;

import io.github.jayclock.smartdomain.core.Entity;
import reengineering.ddd.demo.library.description.BookDescription;

public class Book implements Entity<String, BookDescription> {
  private String identity;
  private BookDescription description;

  public Book(String identity, BookDescription description) {
    this.identity = identity;
    this.description = description;
  }

  @Override
  public String getIdentity() {
    return identity;
  }

  @Override
  public BookDescription getDescription() {
    return description;
  }
}
