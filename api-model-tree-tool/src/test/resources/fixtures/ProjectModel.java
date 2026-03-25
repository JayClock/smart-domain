package fixtures;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;

public class ProjectModel extends RepresentationModel<ProjectModel> {
  public static ProjectModel simple() {
    return new ProjectModel();
  }

  public ProjectModel() {
    add(
        Affordances.of(Link.of("/projects/1").withSelfRel())
            .afford(HttpMethod.DELETE)
            .withName("delete-project")
            .toLink());
    add(Link.of("/projects/1/agents").withRel("agents"));
  }

  public AgentModel firstAgent() {
    return new AgentModel();
  }
}
