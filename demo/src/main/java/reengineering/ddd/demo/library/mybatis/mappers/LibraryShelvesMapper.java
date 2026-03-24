package reengineering.ddd.demo.library.mybatis.mappers;

import io.github.jayclock.smartdomain.mybatis.support.IdHolder;
import java.util.List;
import reengineering.ddd.demo.library.description.ShelfDescription;
import reengineering.ddd.demo.library.model.Shelf;

public interface LibraryShelvesMapper {
  List<Shelf> findShelvesByLibraryId(int libraryId, int from, int limit);

  Shelf findShelfByLibraryAndId(int libraryId, int shelfId);

  int countShelvesByLibrary(int libraryId);

  void insertShelf(IdHolder holder, int libraryId, ShelfDescription description);
}
