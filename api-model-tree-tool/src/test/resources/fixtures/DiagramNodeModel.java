package fixtures;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;

public class DiagramNodeModel extends RepresentationModel<DiagramNodeModel> {
  public DiagramNodeModel() {
    add(
        Affordances.of(Link.of("/projects/1/diagrams/3/nodes/5").withSelfRel())
            .afford(HttpMethod.DELETE)
            .withName("delete-node")
            .toLink());
    add(Link.of("/projects/1/logical-entities/7").withRel("logical-entity"));
    add(Link.of("/projects/1/diagrams/3/nodes").withRel("collection"));
  }

  public LogicalEntityModel logicalEntity() {
    return new LogicalEntityModel();
  }
}
