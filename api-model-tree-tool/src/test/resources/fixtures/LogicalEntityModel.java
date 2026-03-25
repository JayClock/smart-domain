package fixtures;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;

public class LogicalEntityModel extends RepresentationModel<LogicalEntityModel> {
  public LogicalEntityModel() {
    add(
        Affordances.of(Link.of("/projects/1/logical-entities/7").withSelfRel())
            .afford(HttpMethod.DELETE)
            .withName("delete-logical-entity")
            .toLink());
    add(Link.of("/projects/1/logical-entities").withRel("collection"));
  }
}
