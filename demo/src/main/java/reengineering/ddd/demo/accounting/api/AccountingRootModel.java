package reengineering.ddd.demo.accounting.api;

import java.util.List;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import reengineering.ddd.demo.accounting.model.Customer;
import reengineering.ddd.demo.accounting.model.Operator;

public class AccountingRootModel extends RepresentationModel<AccountingRootModel> {
  private final String operatorId;
  private final String operatorName;
  private final String customerId;
  private final String customerName;
  private final List<String> activeRoles;

  private AccountingRootModel(
      String operatorId,
      String operatorName,
      String customerId,
      String customerName,
      List<String> activeRoles) {
    this.operatorId = operatorId;
    this.operatorName = operatorName;
    this.customerId = customerId;
    this.customerName = customerName;
    this.activeRoles = activeRoles;
  }

  public static AccountingRootModel of(
      Operator operator, Customer customer, List<String> activeRoles) {
    AccountingRootModel model =
        new AccountingRootModel(
            operator.getIdentity(),
            operator.getDescription().name(),
            customer.getIdentity(),
            customer.getDescription().name(),
            activeRoles);
    model.add(Link.of(AccountingApiTemplates.root().build().getPath()).withSelfRel());
    model.add(
        Link.of(AccountingApiTemplates.operator(operator.getIdentity()).build().getPath())
            .withRel("operator"));
    model.add(
        Link.of(AccountingApiTemplates.customer(customer.getIdentity()).build().getPath())
            .withRel("customer"));
    return model;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public String getOperatorName() {
    return operatorName;
  }

  public String getCustomerId() {
    return customerId;
  }

  public String getCustomerName() {
    return customerName;
  }

  public List<String> getActiveRoles() {
    return activeRoles;
  }
}
