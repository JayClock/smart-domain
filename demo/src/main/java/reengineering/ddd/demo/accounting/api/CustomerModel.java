package reengineering.ddd.demo.accounting.api;

import java.util.List;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;
import reengineering.ddd.demo.accounting.model.Customer;

public class CustomerModel extends RepresentationModel<CustomerModel> {
  private final String id;
  private final String name;
  private final String email;
  private final List<AccountModel> accounts;
  private final List<SourceEvidenceModel> sourceEvidences;

  private CustomerModel(
      String id,
      String name,
      String email,
      List<AccountModel> accounts,
      List<SourceEvidenceModel> sourceEvidences) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.accounts = accounts;
    this.sourceEvidences = sourceEvidences;
  }

  public static CustomerModel of(Customer customer) {
    String selfHref = AccountingApiTemplates.customer(customer.getIdentity()).build().getPath();
    String createHref =
        AccountingApiTemplates.createSalesSettlement(customer.getIdentity()).build().getPath();
    CustomerModel model =
        new CustomerModel(
            customer.getIdentity(),
            customer.getDescription().name(),
            customer.getDescription().email(),
            customer.accounts().findAll().stream()
                .map(account -> AccountModel.of(customer.getIdentity(), account))
                .toList(),
            customer.sourceEvidences().findAll().stream()
                .map(
                    sourceEvidence ->
                        SourceEvidenceModel.of(customer.getIdentity(), sourceEvidence))
                .toList());
    model.add(Link.of(selfHref).withSelfRel());
    model.add(
        Affordances.of(Link.of(createHref).withRel("source-evidences"))
            .afford(HttpMethod.POST)
            .withInput(AccountingApi.CreateSalesSettlementRequest.class)
            .withName("record-sales-settlement")
            .toLink());
    customer.accounts().findAll().stream()
        .findFirst()
        .ifPresent(
            account ->
                model.add(
                    Link.of(
                            AccountingApiTemplates.account(
                                    customer.getIdentity(), account.getIdentity())
                                .build()
                                .getPath())
                        .withRel("account")));
    customer.sourceEvidences().findAll().stream()
        .findFirst()
        .ifPresent(
            sourceEvidence ->
                model.add(
                    Link.of(
                            AccountingApiTemplates.sourceEvidence(
                                    customer.getIdentity(), sourceEvidence.getIdentity())
                                .build()
                                .getPath())
                        .withRel("source-evidence")));
    return model;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public List<AccountModel> getAccounts() {
    return accounts;
  }

  public List<SourceEvidenceModel> getSourceEvidences() {
    return sourceEvidences;
  }
}
