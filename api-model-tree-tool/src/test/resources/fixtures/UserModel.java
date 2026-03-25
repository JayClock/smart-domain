package fixtures;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;

public class UserModel extends RepresentationModel<UserModel> {
  public record EmbeddedResources(AccountModel account, ProjectModel defaultProject) {}

  public UserModel() {
    add(
        Affordances.of(Link.of("/users/1").withSelfRel())
            .afford(HttpMethod.PUT)
            .withInput(UpdateUserRequest.class)
            .withName("update-user")
            .toLink());
    add(Link.of("/users/1/accounts").withRel("accounts"));
    add(Link.of("/projects/1").withRel("default-project"));
    add(Link.of("/users/1/projects").withRel("projects"));
  }

  public EmbeddedResources embedded() {
    return new EmbeddedResources(new AccountModel(), ProjectModel.simple());
  }

  public static class UpdateUserRequest {}
}
