package fixtures;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;

public class MessageModel extends RepresentationModel<MessageModel> {
  public MessageModel() {
    add(Link.of("/projects/1/conversations/2/messages/8").withSelfRel());
    add(
        Affordances.of(Link.of("/projects/1/conversations/2/messages").withRel("collection"))
            .afford(HttpMethod.POST)
            .withInput(CreateMessageRequest.class)
            .withName("create-message")
            .toLink());
  }

  public static class CreateMessageRequest {}
}
