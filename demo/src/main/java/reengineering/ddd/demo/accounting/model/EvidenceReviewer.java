package reengineering.ddd.demo.accounting.model;

import io.github.jayclock.smartdomain.core.HasMany;
import io.github.jayclock.smartdomain.core.context.ContextRole;
import reengineering.ddd.demo.accounting.description.SalesSettlementDescription;

public interface EvidenceReviewer extends ContextRole<Operator, SourceEvidence<?>> {
  Customer customer();

  default SourceEvidence<?> sourceEvidence() {
    return context();
  }

  default HasMany<String, Transaction> transactions() {
    return sourceEvidence().transactions();
  }

  default Transaction transaction(String transactionId) {
    return transactions()
        .findByIdentity(transactionId)
        .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
  }

  default Account settlementAccount() {
    if (!(sourceEvidence().getDescription() instanceof SalesSettlementDescription description)) {
      throw new IllegalStateException(
          "Unsupported source evidence: " + sourceEvidence().getDescription().getClass());
    }
    return customer()
        .accounts()
        .findByIdentity(description.getAccount().id())
        .orElseThrow(
            () -> new IllegalArgumentException("Account not found: " + description.getAccount()));
  }
}
