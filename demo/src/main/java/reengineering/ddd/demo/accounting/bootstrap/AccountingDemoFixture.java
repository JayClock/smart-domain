package reengineering.ddd.demo.accounting.bootstrap;

import jakarta.annotation.PostConstruct;
import java.util.List;
import org.springframework.stereotype.Component;
import reengineering.ddd.demo.accounting.description.AccountDescription;
import reengineering.ddd.demo.accounting.description.CustomerDescription;
import reengineering.ddd.demo.accounting.description.OperatorDescription;
import reengineering.ddd.demo.accounting.description.SalesSettlementDescription;
import reengineering.ddd.demo.accounting.description.basic.Amount;
import reengineering.ddd.demo.accounting.memory.CustomerAssignments;
import reengineering.ddd.demo.accounting.model.BookkeepingContext;
import reengineering.ddd.demo.accounting.model.Customer;
import reengineering.ddd.demo.accounting.model.Customers;
import reengineering.ddd.demo.accounting.model.Operator;
import reengineering.ddd.demo.accounting.model.Operators;

/** Seeds deterministic data for the runnable accounting demo; it is not a domain service. */
@Component
public final class AccountingDemoFixture {
  private static final String CASH_ACCOUNT_ID = "CASH-001";
  private static final String FIRST_TRANSACTION_ID = "TX-001";
  private static final String FIRST_EVIDENCE_ID = "1";

  private final Operators operators;
  private final Customers customers;
  private final CustomerAssignments assignments;
  private final BookkeepingContext bookkeepingContext;

  private String operatorId;
  private String customerId;

  public AccountingDemoFixture(
      Operators operators,
      Customers customers,
      CustomerAssignments assignments,
      BookkeepingContext bookkeepingContext) {
    this.operators = operators;
    this.customers = customers;
    this.assignments = assignments;
    this.bookkeepingContext = bookkeepingContext;
  }

  @PostConstruct
  void initialize() {
    Operator operator =
        operators.create(new OperatorDescription("Olivia", "Bookkeeper and Auditor"));
    Customer customer =
        customers.create(
            new CustomerDescription("ACME Retail", "finance@acme.example"),
            new Customers.AccountSeed(CASH_ACCOUNT_ID, new AccountDescription(Amount.cny("0.00"))),
            new Customers.AccountSeed("TRANSIT-001", new AccountDescription(Amount.cny("0.00"))));
    assignments.grant(operator, customer);

    bookkeepingContext
        .require(operator, customer)
        .record(
            SalesSettlementDescription.of(
                "ORDER-1001",
                Amount.cny("1000.00"),
                CASH_ACCOUNT_ID,
                new SalesSettlementDescription.Detail(Amount.cny("600.00")),
                new SalesSettlementDescription.Detail(Amount.cny("400.00"))));

    operatorId = operator.getIdentity();
    customerId = customer.getIdentity();
  }

  public String operatorId() {
    return operatorId;
  }

  public String customerId() {
    return customerId;
  }

  public String cashAccountId() {
    return CASH_ACCOUNT_ID;
  }

  public String firstTransactionId() {
    return FIRST_TRANSACTION_ID;
  }

  public String firstEvidenceId() {
    return FIRST_EVIDENCE_ID;
  }

  public List<String> activeRoles(Operator operator, Customer customer) {
    return List.copyOf(assignments.rolesFor(operator, customer));
  }
}
