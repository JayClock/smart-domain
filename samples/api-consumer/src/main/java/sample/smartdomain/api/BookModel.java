package sample.smartdomain.api;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;

public class BookModel extends RepresentationModel<BookModel> {
  private final String id;
  private final String title;
  private final String genre;
  private final BookMetadata metadata;

  private BookModel(String id, String title, String genre, BookMetadata metadata) {
    this.id = id;
    this.title = title;
    this.genre = genre;
    this.metadata = metadata;
  }

  public static BookModel of(Book book, String selfHref) {
    BookModel model = new BookModel(book.id(), book.title(), book.genre(), book.metadata());
    model.add(Link.of(selfHref).withSelfRel());
    return model;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getGenre() {
    return genre;
  }

  public BookMetadata getMetadata() {
    return metadata;
  }
}
