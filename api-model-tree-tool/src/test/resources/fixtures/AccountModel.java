package fixtures;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;

public class AccountModel extends RepresentationModel<AccountModel> {
  public AccountModel() {
    add(
        Affordances.of(Link.of("/accounts/1").withSelfRel())
            .afford(HttpMethod.PUT)
            .withInput(UpdateAccountRequest.class)
            .withName("update-account")
            .toLink());
  }

  public static class UpdateAccountRequest {}
}
