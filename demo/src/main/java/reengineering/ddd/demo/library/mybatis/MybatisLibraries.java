package reengineering.ddd.demo.library.mybatis;

import jakarta.inject.Inject;
import java.util.Optional;
import reengineering.ddd.demo.library.description.LibraryDescription;
import reengineering.ddd.demo.library.model.Libraries;
import reengineering.ddd.demo.library.model.Library;
import reengineering.ddd.demo.library.mybatis.mappers.LibrariesMapper;

public class MybatisLibraries implements Libraries {
  @Inject LibrariesMapper mapper;

  @Override
  public Library create(LibraryDescription description) {
    return mapper.insertLibrary(description);
  }

  @Override
  public Optional<Library> findByIdentity(String identity) {
    return Optional.ofNullable(mapper.findLibraryById(Integer.parseInt(identity)));
  }
}
