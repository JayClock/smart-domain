package reengineering.ddd.demo.accounting.mybatis;

import io.github.jayclock.smartdomain.mybatis.AssociationMapping;
import io.github.jayclock.smartdomain.mybatis.database.EntityList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import reengineering.ddd.demo.accounting.description.TransactionDescription;
import reengineering.ddd.demo.accounting.model.Account;
import reengineering.ddd.demo.accounting.model.SourceEvidence;
import reengineering.ddd.demo.accounting.model.Transaction;

@AssociationMapping(entity = Account.class, field = "transactions", parentIdField = "accountId")
public class AccountTransactions extends EntityList<String, Transaction>
    implements Account.Transactions {
  private String accountId;

  @Autowired private AccountingLedgerMapper mapper;

  public AccountTransactions() {}

  public AccountTransactions(AccountingLedgerMapper mapper, String accountId) {
    this.mapper = mapper;
    this.accountId = accountId;
  }

  @Override
  protected List<Transaction> findEntities(int from, int to) {
    return mapper.findTransactionsByAccountId(accountId, from, to - from);
  }

  @Override
  protected Transaction findEntity(String id) {
    return mapper.findTransactionByAccountAndId(accountId, id);
  }

  @Override
  public int size() {
    return mapper.countTransactionsInAccount(accountId);
  }

  @Override
  public Transaction add(
      Account account, SourceEvidence<?> evidence, TransactionDescription description) {
    return mapper.insertTransaction(account.getIdentity(), evidence.getIdentity(), description);
  }
}
