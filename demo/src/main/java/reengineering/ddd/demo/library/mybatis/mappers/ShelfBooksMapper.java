package reengineering.ddd.demo.library.mybatis.mappers;

import io.github.jayclock.smartdomain.mybatis.support.IdHolder;
import java.util.List;
import reengineering.ddd.demo.library.description.BookDescription;
import reengineering.ddd.demo.library.model.Book;

public interface ShelfBooksMapper {
  List<Book> findBooksByShelfId(int shelfId, int from, int limit);

  Book findBookByShelfAndId(int shelfId, int bookId);

  int countBooksByShelf(int shelfId);

  void insertBook(IdHolder holder, int shelfId, BookDescription description);
}
