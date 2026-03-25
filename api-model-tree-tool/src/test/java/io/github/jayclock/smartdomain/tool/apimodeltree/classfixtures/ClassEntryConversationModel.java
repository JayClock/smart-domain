package io.github.jayclock.smartdomain.tool.apimodeltree.classfixtures;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;

public class ClassEntryConversationModel extends RepresentationModel<ClassEntryConversationModel> {
  public ClassEntryConversationModel() {
    add(
        Affordances.of(Link.of("/class-projects/7/conversations/3").withSelfRel())
            .afford(HttpMethod.DELETE)
            .withName("delete-conversation")
            .toLink());
    add(
        Affordances.of(Link.of("/class-projects/7/conversations/3/messages").withRel("messages"))
            .afford(HttpMethod.POST)
            .withInput(CreateMessageRequest.class)
            .withName("create-message")
            .toLink());
  }

  public static class CreateMessageRequest {}
}
