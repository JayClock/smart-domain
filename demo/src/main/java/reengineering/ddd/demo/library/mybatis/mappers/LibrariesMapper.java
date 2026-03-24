package reengineering.ddd.demo.library.mybatis.mappers;

import reengineering.ddd.demo.library.description.LibraryDescription;
import reengineering.ddd.demo.library.model.Library;

public interface LibrariesMapper {
  Library findLibraryById(int id);

  Library insertLibrary(LibraryDescription description);
}
