package reengineering.ddd.demo.library.mybatis;

import io.github.jayclock.smartdomain.mybatis.AssociationMapping;
import io.github.jayclock.smartdomain.mybatis.database.EntityList;
import io.github.jayclock.smartdomain.mybatis.support.IdHolder;
import jakarta.inject.Inject;
import java.util.List;
import reengineering.ddd.demo.library.description.BookDescription;
import reengineering.ddd.demo.library.model.Book;
import reengineering.ddd.demo.library.model.Shelf;
import reengineering.ddd.demo.library.mybatis.mappers.ShelfBooksMapper;

@AssociationMapping(entity = Shelf.class, field = "books", parentIdField = "shelfId")
public class ShelfBooks extends EntityList<String, Book> implements Shelf.Books {
  private int shelfId;

  @Inject ShelfBooksMapper mapper;

  @Override
  protected List<Book> findEntities(int from, int to) {
    return mapper.findBooksByShelfId(shelfId, from, to - from);
  }

  @Override
  protected Book findEntity(String id) {
    return mapper.findBookByShelfAndId(shelfId, Integer.parseInt(id));
  }

  @Override
  public int size() {
    return mapper.countBooksByShelf(shelfId);
  }

  @Override
  public Book add(BookDescription description) {
    IdHolder holder = new IdHolder();
    mapper.insertBook(holder, shelfId, description);
    return mapper.findBookByShelfAndId(shelfId, holder.id());
  }
}
