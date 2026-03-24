package sample.consumer.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jayclock.smartdomain.core.Entity;
import io.github.jayclock.smartdomain.core.HasMany;
import io.github.jayclock.smartdomain.core.Many;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CoreOnlyConsumerTest {

  @Test
  void should_use_core_contract_without_team_ai_runtime() {
    Book dune = new Book("book-1", "Dune");
    Books books = new Books(List.of(dune));

    assertEquals(1, books.size());
    assertEquals(Optional.of(dune), books.findByIdentity("book-1"));
    assertTrue(books.stream().map(Book::getDescription).toList().contains("Dune"));
  }

  private static final class Book implements Entity<String, String> {
    private final String identity;
    private final String description;

    private Book(String identity, String description) {
      this.identity = identity;
      this.description = description;
    }

    @Override
    public String getIdentity() {
      return identity;
    }

    @Override
    public String getDescription() {
      return description;
    }
  }

  private static final class Books implements Many<Book>, HasMany<String, Book> {
    private final List<Book> books;

    private Books(List<Book> books) {
      this.books = books;
    }

    @Override
    public Many<Book> findAll() {
      return this;
    }

    @Override
    public Optional<Book> findByIdentity(String identifier) {
      return books.stream().filter(book -> book.getIdentity().equals(identifier)).findFirst();
    }

    @Override
    public int size() {
      return books.size();
    }

    @Override
    public Many<Book> subCollection(int from, int to) {
      return new Books(books.subList(from, to));
    }

    @Override
    public Iterator<Book> iterator() {
      return books.iterator();
    }
  }
}
