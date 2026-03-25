package reengineering.ddd.demo.accounting.model;

import io.github.jayclock.smartdomain.core.Entity;
import io.github.jayclock.smartdomain.core.HasOne;
import reengineering.ddd.demo.accounting.description.TransactionDescription;

public class Transaction implements Entity<String, TransactionDescription> {
  private String identity;
  private TransactionDescription description;

  private HasOne<SourceEvidence<?>> sourceEvidence;

  private HasOne<Account> account;

  private Transaction() {}

  public Transaction(
      String identity,
      TransactionDescription description,
      HasOne<Account> account,
      HasOne<SourceEvidence<?>> sourceEvidence) {
    this.identity = identity;
    this.description = description;
    this.account = account;
    this.sourceEvidence = sourceEvidence;
  }

  @Override
  public String getIdentity() {
    return identity;
  }

  @Override
  public TransactionDescription getDescription() {
    return description;
  }

  public SourceEvidence<?> sourceEvidence() {
    return sourceEvidence.get();
  }

  public Account account() {
    return account.get();
  }
}
