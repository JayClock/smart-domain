package io.github.jayclock.smartdomain.tool.apimodeltree.classfixtures;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;

public class ClassEntryProjectModel extends RepresentationModel<ClassEntryProjectModel> {
  public ClassEntryProjectModel() {
    add(
        Affordances.of(Link.of("/class-projects/7").withSelfRel())
            .afford(HttpMethod.DELETE)
            .withName("delete-project")
            .toLink());
    add(Link.of("/class-projects/7/conversations").withRel("conversations"));
  }

  public ClassEntryConversationModel firstConversation() {
    return new ClassEntryConversationModel();
  }
}
