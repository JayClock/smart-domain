package reengineering.ddd.demo.accounting.api;

import java.util.List;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import reengineering.ddd.demo.accounting.description.SalesSettlementDescription;
import reengineering.ddd.demo.accounting.model.SourceEvidence;

public class SourceEvidenceModel extends RepresentationModel<SourceEvidenceModel> {
  private final String id;
  private final String type;
  private final String orderId;
  private final String total;
  private final String accountId;
  private final List<TransactionModel> transactions;

  private SourceEvidenceModel(
      String id,
      String type,
      String orderId,
      String total,
      String accountId,
      List<TransactionModel> transactions) {
    this.id = id;
    this.type = type;
    this.orderId = orderId;
    this.total = total;
    this.accountId = accountId;
    this.transactions = transactions;
  }

  public static SourceEvidenceModel of(String customerId, SourceEvidence<?> sourceEvidence) {
    if (!(sourceEvidence.getDescription() instanceof SalesSettlementDescription description)) {
      throw new IllegalArgumentException(
          "Unsupported source evidence: " + sourceEvidence.getDescription());
    }

    SourceEvidenceModel model =
        new SourceEvidenceModel(
            sourceEvidence.getIdentity(),
            "sales-settlement",
            description.getOrder().id(),
            description.getTotal().value().toPlainString(),
            description.getAccount().id(),
            sourceEvidence.transactions().findAll().stream()
                .map(
                    transaction ->
                        TransactionModel.of(
                            transaction,
                            customerId,
                            AccountingApiTemplates.transaction(
                                    customerId,
                                    transaction.account().getIdentity(),
                                    transaction.getIdentity())
                                .build()
                                .getPath(),
                            AccountingApiTemplates.sourceEvidence(
                                    customerId, sourceEvidence.getIdentity())
                                .build()
                                .getPath()))
                .toList());
    model.add(
        Link.of(
                AccountingApiTemplates.sourceEvidence(customerId, sourceEvidence.getIdentity())
                    .build()
                    .getPath())
            .withSelfRel());
    model.add(
        Link.of(
                AccountingApiTemplates.account(customerId, description.getAccount().id())
                    .build()
                    .getPath())
            .withRel("account"));
    sourceEvidence.transactions().findAll().stream()
        .findFirst()
        .ifPresent(
            transaction ->
                model.add(
                    Link.of(
                            AccountingApiTemplates.transaction(
                                    customerId,
                                    transaction.account().getIdentity(),
                                    transaction.getIdentity())
                                .build()
                                .getPath())
                        .withRel("transaction")));
    return model;
  }

  public String getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public String getOrderId() {
    return orderId;
  }

  public String getTotal() {
    return total;
  }

  public String getAccountId() {
    return accountId;
  }

  public List<TransactionModel> getTransactions() {
    return transactions;
  }
}
