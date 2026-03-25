package reengineering.ddd.demo.accounting.model;

import io.github.jayclock.smartdomain.core.Entity;
import io.github.jayclock.smartdomain.core.HasMany;
import java.util.List;
import java.util.Map;
import reengineering.ddd.demo.accounting.description.SourceEvidenceDescription;
import reengineering.ddd.demo.accounting.description.TransactionDescription;

public interface SourceEvidence<Description extends SourceEvidenceDescription>
    extends Entity<String, Description> {

  interface Transactions extends HasMany<String, Transaction> {}

  HasMany<String, Transaction> transactions();

  Map<String, List<TransactionDescription>> toTransactions();
}
