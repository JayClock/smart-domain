package reengineering.ddd.demo.ecommerce.model;

import io.github.jayclock.smartdomain.core.Entity;
import reengineering.ddd.demo.ecommerce.description.UserDescription;

public class User implements Entity<String, UserDescription> {
  private String identity;
  private UserDescription description;

  public User(String identity, UserDescription description) {
    this.identity = identity;
    this.description = description;
  }

  @Override
  public String getIdentity() {
    return identity;
  }

  @Override
  public UserDescription getDescription() {
    return description;
  }
}
