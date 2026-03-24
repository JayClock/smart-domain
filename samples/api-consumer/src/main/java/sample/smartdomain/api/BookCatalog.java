package sample.smartdomain.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class BookCatalog {
  private final Map<String, Book> books = new LinkedHashMap<>();

  public BookCatalog() {
    Book sample =
        new Book(
            "book-1",
            "Smart Domain in Practice",
            "reference",
            new BookMetadata("architects", "advanced"));
    books.put(sample.id(), sample);
  }

  public List<Book> findAll() {
    return new ArrayList<>(books.values());
  }

  public Book findById(String id) {
    return books.get(id);
  }

  public Book create(String title, String genre, BookMetadata metadata) {
    String id = "book-" + (books.size() + 1);
    Book created = new Book(id, title, genre, metadata);
    books.put(id, created);
    return created;
  }
}
