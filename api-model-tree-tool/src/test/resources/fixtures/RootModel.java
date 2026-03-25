package fixtures;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;

public class RootModel extends RepresentationModel<RootModel> {
  public RootModel() {
    add(Link.of("/").withSelfRel());
    add(Link.of("/users/1").withRel("me"));
    add(Link.of("/logout", "logout"));
  }
}
