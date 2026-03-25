package reengineering.ddd.demo.accounting.memory;

import java.util.Optional;
import org.springframework.stereotype.Component;
import reengineering.ddd.demo.accounting.model.Customer;
import reengineering.ddd.demo.accounting.model.EvidenceReviewContext;
import reengineering.ddd.demo.accounting.model.EvidenceReviewer;
import reengineering.ddd.demo.accounting.model.Operator;
import reengineering.ddd.demo.accounting.model.SourceEvidence;

@Component
public class DefaultEvidenceReviewContext implements EvidenceReviewContext {
  private final CustomerAssignments assignments;
  private final InMemoryCustomers customers;

  public DefaultEvidenceReviewContext(
      CustomerAssignments assignments, InMemoryCustomers customers) {
    this.assignments = assignments;
    this.customers = customers;
  }

  @Override
  public Optional<EvidenceReviewer> switchTo(Operator actor, SourceEvidence<?> context) {
    return customers
        .findCustomerBySourceEvidence(context.getIdentity())
        .filter(
            customer ->
                assignments.canAssume(actor, customer, CustomerAssignments.EVIDENCE_REVIEWER))
        .map(customer -> new DefaultEvidenceReviewer(actor, customer, context));
  }

  private record DefaultEvidenceReviewer(
      Operator actor, Customer customer, SourceEvidence<?> context) implements EvidenceReviewer {}
}
