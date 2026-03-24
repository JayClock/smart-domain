package reengineering.ddd.demo.ecommerce.api;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import reengineering.ddd.demo.ecommerce.model.User;

public class UserModel extends RepresentationModel<UserModel> {
  private final String id;
  private final String name;

  private UserModel(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public static UserModel of(User user, String href) {
    UserModel model = new UserModel(user.getIdentity(), user.getDescription().name());
    model.add(Link.of(href).withSelfRel());
    return model;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
