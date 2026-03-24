package reengineering.ddd.demo.ecommerce.memory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import reengineering.ddd.demo.ecommerce.description.UserDescription;
import reengineering.ddd.demo.ecommerce.model.User;
import reengineering.ddd.demo.ecommerce.model.Users;

@Component
public class InMemoryUsers implements Users {
  private final Map<String, UserDescription> users = new LinkedHashMap<>();
  private int nextId = 1;

  @Override
  public User create(UserDescription description) {
    String identity = String.valueOf(nextId++);
    users.put(identity, description);
    return new User(identity, description);
  }

  @Override
  public Optional<User> findByIdentity(String identity) {
    return Optional.ofNullable(users.get(identity)).map(description -> new User(identity, description));
  }
}
