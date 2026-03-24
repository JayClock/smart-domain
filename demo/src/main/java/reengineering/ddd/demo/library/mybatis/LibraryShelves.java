package reengineering.ddd.demo.library.mybatis;

import io.github.jayclock.smartdomain.mybatis.AssociationMapping;
import io.github.jayclock.smartdomain.mybatis.database.EntityList;
import io.github.jayclock.smartdomain.mybatis.support.IdHolder;
import jakarta.inject.Inject;
import java.util.List;
import reengineering.ddd.demo.library.description.ShelfDescription;
import reengineering.ddd.demo.library.model.Library;
import reengineering.ddd.demo.library.model.Shelf;
import reengineering.ddd.demo.library.mybatis.mappers.LibraryShelvesMapper;

@AssociationMapping(entity = Library.class, field = "shelves", parentIdField = "libraryId")
public class LibraryShelves extends EntityList<String, Shelf> implements Library.Shelves {
  private int libraryId;

  @Inject LibraryShelvesMapper mapper;

  @Override
  protected List<Shelf> findEntities(int from, int to) {
    return mapper.findShelvesByLibraryId(libraryId, from, to - from);
  }

  @Override
  protected Shelf findEntity(String id) {
    return mapper.findShelfByLibraryAndId(libraryId, Integer.parseInt(id));
  }

  @Override
  public int size() {
    return mapper.countShelvesByLibrary(libraryId);
  }

  @Override
  public Shelf add(ShelfDescription description) {
    IdHolder holder = new IdHolder();
    mapper.insertShelf(holder, libraryId, description);
    return mapper.findShelfByLibraryAndId(libraryId, holder.id());
  }
}
