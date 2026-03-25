package reengineering.ddd.demo.accounting.model;

import io.github.jayclock.smartdomain.core.Entity;
import io.github.jayclock.smartdomain.core.HasMany;
import java.util.List;
import java.util.Map;
import reengineering.ddd.demo.accounting.description.CustomerDescription;
import reengineering.ddd.demo.accounting.description.SourceEvidenceDescription;
import reengineering.ddd.demo.accounting.description.TransactionDescription;

public class Customer implements Entity<String, CustomerDescription> {
  private String identity;
  private CustomerDescription description;

  private SourceEvidences sourceEvidences;

  private Accounts accounts;

  public Customer(
      String identity,
      CustomerDescription description,
      SourceEvidences sourceEvidences,
      Accounts accounts) {
    this.identity = identity;
    this.description = description;
    this.sourceEvidences = sourceEvidences;
    this.accounts = accounts;
  }

  private Customer() {}

  public String getIdentity() {
    return identity;
  }

  public CustomerDescription getDescription() {
    return description;
  }

  public HasMany<String, SourceEvidence<?>> sourceEvidences() {
    return sourceEvidences;
  }

  public HasMany<String, Account> accounts() {
    return accounts;
  }

  public SourceEvidence<?> add(SourceEvidenceDescription description) {
    SourceEvidence<?> evidence = sourceEvidences.add(description);
    Map<String, List<TransactionDescription>> transactions = evidence.toTransactions();
    for (String accountId : transactions.keySet()) {
      Account account =
          accounts
              .findByIdentity(accountId)
              .orElseThrow(() -> new AccountNotFoundException(accountId));
      accounts.update(account, account.add(evidence, transactions.get(accountId)));
    }
    return evidence;
  }

  public SourceEvidence<?> record(SourceEvidenceDescription description) {
    return add(description);
  }

  public interface SourceEvidences extends HasMany<String, SourceEvidence<?>> {
    SourceEvidence<?> add(SourceEvidenceDescription description);
  }

  public interface Accounts extends HasMany<String, Account> {
    void update(Account account, Account.AccountChange change);
  }
}
