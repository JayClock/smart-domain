package reengineering.ddd.demo.accounting.model;

import io.github.jayclock.smartdomain.core.HasMany;
import io.github.jayclock.smartdomain.core.context.ContextRole;

public interface Accountant extends ContextRole<Operator, Account> {
  Customer customer();

  default Account account() {
    return context();
  }

  default HasMany<String, Transaction> transactions() {
    return account().transactions();
  }

  default Transaction transaction(String transactionId) {
    return transactions()
        .findByIdentity(transactionId)
        .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
  }

  default SourceEvidence<?> sourceEvidence(String transactionId) {
    return transaction(transactionId).sourceEvidence();
  }
}
