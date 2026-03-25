package reengineering.ddd.demo.accounting.api;

import java.util.List;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import reengineering.ddd.demo.accounting.model.Account;

public class AccountModel extends RepresentationModel<AccountModel> {
  private final String id;
  private final String current;
  private final String currency;
  private final List<TransactionModel> transactions;

  private AccountModel(
      String id, String current, String currency, List<TransactionModel> transactions) {
    this.id = id;
    this.current = current;
    this.currency = currency;
    this.transactions = transactions;
  }

  public static AccountModel of(String customerId, Account account) {
    AccountModel model =
        new AccountModel(
            account.getIdentity(),
            account.getDescription().current().value().toPlainString(),
            account.getDescription().current().currency().name(),
            account.transactions().findAll().stream()
                .map(
                    transaction ->
                        TransactionModel.of(
                            transaction,
                            customerId,
                            AccountingApiTemplates.transaction(
                                    customerId, account.getIdentity(), transaction.getIdentity())
                                .build()
                                .getPath(),
                            AccountingApiTemplates.sourceEvidence(
                                    customerId, transaction.sourceEvidence().getIdentity())
                                .build()
                                .getPath()))
                .toList());
    model.add(
        Link.of(AccountingApiTemplates.account(customerId, account.getIdentity()).build().getPath())
            .withSelfRel());
    account.transactions().findAll().stream()
        .findFirst()
        .ifPresent(
            transaction ->
                model.add(
                    Link.of(
                            AccountingApiTemplates.transaction(
                                    customerId, account.getIdentity(), transaction.getIdentity())
                                .build()
                                .getPath())
                        .withRel("transaction")));
    return model;
  }

  public String getId() {
    return id;
  }

  public String getCurrent() {
    return current;
  }

  public String getCurrency() {
    return currency;
  }

  public List<TransactionModel> getTransactions() {
    return transactions;
  }
}
