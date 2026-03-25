package fixtures;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;

public class DiagramModel extends RepresentationModel<DiagramModel> {
  public DiagramModel() {
    add(Link.of("/projects/1").withRel("project"));
    add(Link.of("/projects/1/diagrams").withRel("collection"));
    add(
        Affordances.of(Link.of("/projects/1/diagrams/3").withSelfRel())
            .afford(HttpMethod.DELETE)
            .withName("delete-diagram")
            .toLink());
    add(
        Affordances.of(Link.of("/projects/1/diagrams/3/nodes").withRel("nodes"))
            .afford(HttpMethod.POST)
            .withInput(CreateNodeRequest.class)
            .withName("create-node")
            .toLink());
  }

  public DiagramNodeModel firstNode() {
    return new DiagramNodeModel();
  }

  public static class CreateNodeRequest {}
}
