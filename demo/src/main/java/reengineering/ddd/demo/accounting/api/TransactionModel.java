package reengineering.ddd.demo.accounting.api;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import reengineering.ddd.demo.accounting.model.Transaction;

public class TransactionModel extends RepresentationModel<TransactionModel> {
  private final String id;
  private final String accountId;
  private final String sourceEvidenceId;
  private final String amount;
  private final String currency;
  private final String createdAt;

  private TransactionModel(
      String id,
      String accountId,
      String sourceEvidenceId,
      String amount,
      String currency,
      String createdAt) {
    this.id = id;
    this.accountId = accountId;
    this.sourceEvidenceId = sourceEvidenceId;
    this.amount = amount;
    this.currency = currency;
    this.createdAt = createdAt;
  }

  public static TransactionModel of(
      Transaction transaction, String customerId, String href, String sourceEvidenceHref) {
    TransactionModel model =
        new TransactionModel(
            transaction.getIdentity(),
            transaction.account().getIdentity(),
            transaction.sourceEvidence().getIdentity(),
            transaction.getDescription().amount().value().toPlainString(),
            transaction.getDescription().amount().currency().name(),
            transaction.getDescription().createdAt().toString());
    model.add(Link.of(href).withSelfRel());
    model.add(
        Link.of(
                AccountingApiTemplates.account(customerId, transaction.account().getIdentity())
                    .build()
                    .getPath())
            .withRel("account"));
    model.add(Link.of(sourceEvidenceHref).withRel("source-evidence"));
    return model;
  }

  public String getId() {
    return id;
  }

  public String getAccountId() {
    return accountId;
  }

  public String getSourceEvidenceId() {
    return sourceEvidenceId;
  }

  public String getAmount() {
    return amount;
  }

  public String getCurrency() {
    return currency;
  }

  public String getCreatedAt() {
    return createdAt;
  }
}
