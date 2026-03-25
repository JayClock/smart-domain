package io.github.jayclock.smartdomain.tool.apimodeltree.classfixtures;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;

public class ClassEntryUserModel extends RepresentationModel<ClassEntryUserModel> {
  public ClassEntryUserModel() {
    add(
        Affordances.of(Link.of("/class-users/1").withSelfRel())
            .afford(HttpMethod.PUT)
            .withInput(UpdateUserRequest.class)
            .withName("update-user")
            .toLink());
    add(Link.of("/class-users/1/projects").withRel("projects"));
  }

  public ClassEntryProjectModel defaultProject() {
    return new ClassEntryProjectModel();
  }

  public static class UpdateUserRequest {}
}
