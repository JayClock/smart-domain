package reengineering.ddd.demo.accounting.memory;

import java.util.Optional;
import org.springframework.stereotype.Component;
import reengineering.ddd.demo.accounting.model.AuditContext;
import reengineering.ddd.demo.accounting.model.Auditor;
import reengineering.ddd.demo.accounting.model.Customer;
import reengineering.ddd.demo.accounting.model.Operator;

@Component
public class DefaultAuditContext implements AuditContext {
  private final CustomerAssignments assignments;

  public DefaultAuditContext(CustomerAssignments assignments) {
    this.assignments = assignments;
  }

  @Override
  public Optional<Auditor> switchTo(Operator actor, Customer context) {
    if (!assignments.canAssume(actor, context, CustomerAssignments.AUDITOR)) {
      return Optional.empty();
    }
    return Optional.of(new DefaultAuditor(actor, context));
  }

  private record DefaultAuditor(Operator actor, Customer context) implements Auditor {}
}
