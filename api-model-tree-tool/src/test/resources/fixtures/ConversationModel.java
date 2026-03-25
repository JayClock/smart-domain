package fixtures;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;

public class ConversationModel extends RepresentationModel<ConversationModel> {
  public ConversationModel() {
    add(
        Affordances.of(Link.of("/projects/1/conversations/2").withSelfRel())
            .afford(HttpMethod.DELETE)
            .withName("delete-conversation")
            .toLink());
    add(
        Affordances.of(Link.of("/projects/1/conversations/2/messages/stream").withRel("chat"))
            .afford(HttpMethod.POST)
            .withInput(ChatRequest.class)
            .withName("chat")
            .toLink());
    add(Link.of("/projects/1/conversations/2/messages").withRel("messages"));
    add(Link.of("/projects/1/conversations").withRel("collection"));
  }

  public MessageModel latestMessage() {
    return new MessageModel();
  }

  public static class ChatRequest {}
}
