package reengineering.ddd.demo.accounting.memory;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import reengineering.ddd.demo.accounting.model.Customer;
import reengineering.ddd.demo.accounting.model.Operator;

@Component
public class CustomerAssignments {
  public static final String BOOKKEEPER = "bookkeeper";
  public static final String AUDITOR = "auditor";
  public static final String ACCOUNTANT = "accountant";
  public static final String EVIDENCE_REVIEWER = "evidence-reviewer";

  private final Map<String, Set<ContextGrant>> operatorGrants = new LinkedHashMap<>();

  public void grant(Operator operator, Customer customer) {
    grant(operator, customer, BOOKKEEPER, AUDITOR, ACCOUNTANT, EVIDENCE_REVIEWER);
  }

  public void grant(Operator operator, Customer customer, String... roles) {
    Set<ContextGrant> grants =
        operatorGrants.computeIfAbsent(operator.getIdentity(), ignored -> new LinkedHashSet<>());
    for (String role : roles) {
      grants.add(new ContextGrant(customer.getIdentity(), role));
    }
  }

  public boolean canAccess(Operator operator, Customer customer) {
    return !rolesFor(operator, customer).isEmpty();
  }

  public boolean canAssume(Operator operator, Customer customer, String role) {
    return operatorGrants.getOrDefault(operator.getIdentity(), Set.of()).stream()
        .anyMatch(
            grant ->
                grant.customerId().equals(customer.getIdentity()) && grant.role().equals(role));
  }

  public Set<String> rolesFor(Operator operator, Customer customer) {
    return operatorGrants.getOrDefault(operator.getIdentity(), Set.of()).stream()
        .filter(grant -> grant.customerId().equals(customer.getIdentity()))
        .map(ContextGrant::role)
        .collect(LinkedHashSet::new, Set::add, Set::addAll);
  }

  private record ContextGrant(String customerId, String role) {}
}
