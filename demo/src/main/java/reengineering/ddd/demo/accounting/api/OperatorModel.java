package reengineering.ddd.demo.accounting.api;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import reengineering.ddd.demo.accounting.model.Operator;

public class OperatorModel extends RepresentationModel<OperatorModel> {
  private final String id;
  private final String name;
  private final String title;

  private OperatorModel(String id, String name, String title) {
    this.id = id;
    this.name = name;
    this.title = title;
  }

  public static OperatorModel of(Operator operator, String customerId) {
    OperatorModel model =
        new OperatorModel(
            operator.getIdentity(),
            operator.getDescription().name(),
            operator.getDescription().title());
    model.add(
        Link.of(AccountingApiTemplates.operator(operator.getIdentity()).build().getPath())
            .withSelfRel());
    model.add(
        Link.of(AccountingApiTemplates.customer(customerId).build().getPath()).withRel("customer"));
    return model;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getTitle() {
    return title;
  }
}
