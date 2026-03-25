package reengineering.ddd.demo.accounting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import org.junit.jupiter.api.Test;
import reengineering.ddd.demo.accounting.description.AccountDescription;
import reengineering.ddd.demo.accounting.description.CustomerDescription;
import reengineering.ddd.demo.accounting.description.OperatorDescription;
import reengineering.ddd.demo.accounting.description.SalesSettlementDescription;
import reengineering.ddd.demo.accounting.description.basic.Amount;
import reengineering.ddd.demo.accounting.memory.CustomerAssignments;
import reengineering.ddd.demo.accounting.memory.DefaultAccountContext;
import reengineering.ddd.demo.accounting.memory.DefaultAuditContext;
import reengineering.ddd.demo.accounting.memory.DefaultBookkeepingContext;
import reengineering.ddd.demo.accounting.memory.DefaultEvidenceReviewContext;
import reengineering.ddd.demo.accounting.memory.InMemoryCustomers;
import reengineering.ddd.demo.accounting.memory.InMemoryOperators;
import reengineering.ddd.demo.accounting.memory.SourceEvidenceTransactions;
import reengineering.ddd.demo.accounting.model.Customers;
import reengineering.ddd.demo.accounting.mybatis.AccountTransactions;

class AccountingDemoTest {

  @Test
  void should_switch_operator_into_customer_account_and_evidence_roles() {
    InMemoryOperators operators = new InMemoryOperators();
    InMemoryCustomers customers = new InMemoryCustomers();
    CustomerAssignments assignments = new CustomerAssignments();

    var operator = operators.create(new OperatorDescription("Olivia", "Bookkeeper"));
    var customer =
        customers.create(
            new CustomerDescription("ACME Retail", "finance@acme.example"),
            new Customers.AccountSeed("CASH-001", new AccountDescription(Amount.cny("0.00"))));
    assignments.grant(operator, customer);

    var bookkeeper = new DefaultBookkeepingContext(assignments).require(operator, customer);
    var auditor = new DefaultAuditContext(assignments).require(operator, customer);

    var evidence =
        bookkeeper.record(
            SalesSettlementDescription.of(
                "ORDER-1001",
                Amount.cny("1000.00"),
                "CASH-001",
                new SalesSettlementDescription.Detail(Amount.cny("600.00")),
                new SalesSettlementDescription.Detail(Amount.cny("400.00"))));

    var account = auditor.account("CASH-001");
    var accountant = new DefaultAccountContext(assignments, customers).require(operator, account);
    var reviewer =
        new DefaultEvidenceReviewContext(assignments, customers).require(operator, evidence);

    assertInstanceOf(SourceEvidenceTransactions.class, evidence.transactions());
    assertInstanceOf(AccountTransactions.class, account.transactions());
    assertEquals("ACME Retail", bookkeeper.customer().getDescription().name());
    assertEquals("CASH-001", account.getIdentity());
    assertEquals("ACME Retail", accountant.customer().getDescription().name());
    assertEquals("CASH-001", accountant.account().getIdentity());
    assertEquals("1", reviewer.sourceEvidence().getIdentity());
    assertEquals("CASH-001", reviewer.settlementAccount().getIdentity());
    assertEquals("1000.00", account.getDescription().current().value().toPlainString());
    assertEquals(2, auditor.transactions("CASH-001").findAll().size());
    assertEquals(2, accountant.transactions().findAll().size());
    assertEquals(2, reviewer.transactions().findAll().size());
    assertEquals("1", accountant.sourceEvidence("TX-001").getIdentity());
    assertEquals("TX-001", reviewer.transaction("TX-001").getIdentity());
    assertEquals(
        List.of("TX-001", "TX-002"),
        account.transactions().findAll().stream().map(it -> it.getIdentity()).toList());
  }
}
