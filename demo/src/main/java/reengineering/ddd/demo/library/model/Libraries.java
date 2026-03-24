package reengineering.ddd.demo.library.model;

import java.util.Optional;
import reengineering.ddd.demo.library.description.LibraryDescription;

public interface Libraries {
  Library create(LibraryDescription description);

  Optional<Library> findByIdentity(String identity);
}
