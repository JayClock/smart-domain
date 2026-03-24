package reengineering.ddd.demo.ecommerce.model;

import java.util.Optional;
import reengineering.ddd.demo.ecommerce.description.UserDescription;

public interface Users {
  User create(UserDescription description);

  Optional<User> findByIdentity(String identity);
}
