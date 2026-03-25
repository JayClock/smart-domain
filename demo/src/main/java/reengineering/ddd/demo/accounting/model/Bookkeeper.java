package reengineering.ddd.demo.accounting.model;

import io.github.jayclock.smartdomain.core.HasMany;
import io.github.jayclock.smartdomain.core.context.ContextRole;
import reengineering.ddd.demo.accounting.description.SourceEvidenceDescription;

public interface Bookkeeper extends ContextRole<Operator, Customer> {
  default Customer customer() {
    return context();
  }

  default HasMany<String, Account> accounts() {
    return customer().accounts();
  }

  default HasMany<String, SourceEvidence<?>> sourceEvidences() {
    return customer().sourceEvidences();
  }

  default SourceEvidence<?> record(SourceEvidenceDescription description) {
    return customer().record(description);
  }
}
