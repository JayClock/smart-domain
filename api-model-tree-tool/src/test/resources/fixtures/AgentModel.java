package fixtures;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;

public class AgentModel extends RepresentationModel<AgentModel> {
  public AgentModel() {
    add(
        Affordances.of(Link.of("/agents/1").withSelfRel())
            .afford(HttpMethod.PUT)
            .withInput(UpdateAgentRequest.class)
            .withName("update-agent-status")
            .andAfford(HttpMethod.DELETE)
            .withName("delete-agent")
            .toLink());
    add(Link.of("/projects/1").withRel("project"));
  }

  public ProjectModel ownerProject() {
    return ProjectModel.simple();
  }

  public static class UpdateAgentRequest {}
}
