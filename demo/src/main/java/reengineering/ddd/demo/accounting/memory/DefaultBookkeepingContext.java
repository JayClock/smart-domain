package reengineering.ddd.demo.accounting.memory;

import java.util.Optional;
import org.springframework.stereotype.Component;
import reengineering.ddd.demo.accounting.model.Bookkeeper;
import reengineering.ddd.demo.accounting.model.BookkeepingContext;
import reengineering.ddd.demo.accounting.model.Customer;
import reengineering.ddd.demo.accounting.model.Operator;

@Component
public class DefaultBookkeepingContext implements BookkeepingContext {
  private final CustomerAssignments assignments;

  public DefaultBookkeepingContext(CustomerAssignments assignments) {
    this.assignments = assignments;
  }

  @Override
  public Optional<Bookkeeper> switchTo(Operator actor, Customer context) {
    if (!assignments.canAssume(actor, context, CustomerAssignments.BOOKKEEPER)) {
      return Optional.empty();
    }
    return Optional.of(new DefaultBookkeeper(actor, context));
  }

  private record DefaultBookkeeper(Operator actor, Customer context) implements Bookkeeper {}
}
