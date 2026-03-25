package reengineering.ddd.demo.accounting.memory;

import java.util.Optional;
import org.springframework.stereotype.Component;
import reengineering.ddd.demo.accounting.model.Account;
import reengineering.ddd.demo.accounting.model.AccountContext;
import reengineering.ddd.demo.accounting.model.Accountant;
import reengineering.ddd.demo.accounting.model.Customer;
import reengineering.ddd.demo.accounting.model.Operator;

@Component
public class DefaultAccountContext implements AccountContext {
  private final CustomerAssignments assignments;
  private final InMemoryCustomers customers;

  public DefaultAccountContext(CustomerAssignments assignments, InMemoryCustomers customers) {
    this.assignments = assignments;
    this.customers = customers;
  }

  @Override
  public Optional<Accountant> switchTo(Operator actor, Account context) {
    return customers
        .findCustomerByAccount(context.getIdentity())
        .filter(customer -> assignments.canAssume(actor, customer, CustomerAssignments.ACCOUNTANT))
        .map(customer -> new DefaultAccountant(actor, customer, context));
  }

  private record DefaultAccountant(Operator actor, Customer customer, Account context)
      implements Accountant {}
}
