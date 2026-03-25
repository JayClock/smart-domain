package reengineering.ddd.demo.accounting.api;

import jakarta.annotation.PostConstruct;
import java.util.List;
import org.springframework.stereotype.Component;
import reengineering.ddd.demo.accounting.description.AccountDescription;
import reengineering.ddd.demo.accounting.description.CustomerDescription;
import reengineering.ddd.demo.accounting.description.OperatorDescription;
import reengineering.ddd.demo.accounting.description.SalesSettlementDescription;
import reengineering.ddd.demo.accounting.description.basic.Amount;
import reengineering.ddd.demo.accounting.memory.CustomerAssignments;
import reengineering.ddd.demo.accounting.model.Account;
import reengineering.ddd.demo.accounting.model.AccountContext;
import reengineering.ddd.demo.accounting.model.Accountant;
import reengineering.ddd.demo.accounting.model.AuditContext;
import reengineering.ddd.demo.accounting.model.Auditor;
import reengineering.ddd.demo.accounting.model.Bookkeeper;
import reengineering.ddd.demo.accounting.model.BookkeepingContext;
import reengineering.ddd.demo.accounting.model.Customer;
import reengineering.ddd.demo.accounting.model.Customers;
import reengineering.ddd.demo.accounting.model.EvidenceReviewContext;
import reengineering.ddd.demo.accounting.model.EvidenceReviewer;
import reengineering.ddd.demo.accounting.model.Operator;
import reengineering.ddd.demo.accounting.model.Operators;
import reengineering.ddd.demo.accounting.model.SourceEvidence;

@Component
public class AccountingDemoFacade {
  private final Operators operators;
  private final Customers customers;
  private final CustomerAssignments assignments;
  private final BookkeepingContext bookkeepingContext;
  private final AuditContext auditContext;
  private final AccountContext accountContext;
  private final EvidenceReviewContext evidenceReviewContext;

  private Operator demoOperator;
  private Customer demoCustomer;

  public AccountingDemoFacade(
      Operators operators,
      Customers customers,
      CustomerAssignments assignments,
      BookkeepingContext bookkeepingContext,
      AuditContext auditContext,
      AccountContext accountContext,
      EvidenceReviewContext evidenceReviewContext) {
    this.operators = operators;
    this.customers = customers;
    this.assignments = assignments;
    this.bookkeepingContext = bookkeepingContext;
    this.auditContext = auditContext;
    this.accountContext = accountContext;
    this.evidenceReviewContext = evidenceReviewContext;
  }

  @PostConstruct
  void initialize() {
    demoOperator = operators.create(new OperatorDescription("Olivia", "Bookkeeper and Auditor"));
    demoCustomer =
        customers.create(
            new CustomerDescription("ACME Retail", "finance@acme.example"),
            new Customers.AccountSeed("CASH-001", new AccountDescription(Amount.cny("0.00"))),
            new Customers.AccountSeed("TRANSIT-001", new AccountDescription(Amount.cny("0.00"))));
    assignments.grant(demoOperator, demoCustomer);

    recordSalesSettlement(
        "ORDER-1001", "CASH-001", List.of(Amount.cny("600.00"), Amount.cny("400.00")));
  }

  public Operator operator() {
    return demoOperator;
  }

  public Customer customer() {
    return demoCustomer;
  }

  public Bookkeeper bookkeeper() {
    return bookkeepingContext.require(demoOperator, demoCustomer);
  }

  public Auditor auditor() {
    return auditContext.require(demoOperator, demoCustomer);
  }

  public Accountant accountant(String accountId) {
    return accountContext.require(demoOperator, account(accountId));
  }

  public EvidenceReviewer evidenceReviewer(String evidenceId) {
    return evidenceReviewContext.require(demoOperator, sourceEvidence(evidenceId));
  }

  public SourceEvidence<?> recordSalesSettlement(
      String orderId, String accountId, List<Amount> detailAmounts) {
    Amount total = Amount.sum(detailAmounts.toArray(Amount[]::new));
    SalesSettlementDescription.Detail[] details =
        detailAmounts.stream()
            .map(SalesSettlementDescription.Detail::new)
            .toArray(SalesSettlementDescription.Detail[]::new);
    return bookkeeper().record(SalesSettlementDescription.of(orderId, total, accountId, details));
  }

  public Account account(String accountId) {
    return auditor().account(accountId);
  }

  public SourceEvidence<?> sourceEvidence(String evidenceId) {
    return customer()
        .sourceEvidences()
        .findByIdentity(evidenceId)
        .orElseThrow(
            () -> new IllegalArgumentException("Source evidence not found: " + evidenceId));
  }

  public List<String> activeRoles() {
    return List.copyOf(assignments.rolesFor(demoOperator, demoCustomer));
  }
}
