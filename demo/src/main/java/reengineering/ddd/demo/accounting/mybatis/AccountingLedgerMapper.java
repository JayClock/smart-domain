package reengineering.ddd.demo.accounting.mybatis;

import java.util.List;
import reengineering.ddd.demo.accounting.description.TransactionDescription;
import reengineering.ddd.demo.accounting.model.Transaction;

public interface AccountingLedgerMapper {
  List<Transaction> findTransactionsByAccountId(String accountId, int from, int limit);

  Transaction findTransactionByAccountAndId(String accountId, String transactionId);

  int countTransactionsInAccount(String accountId);

  Transaction insertTransaction(
      String accountId, String evidenceId, TransactionDescription description);

  List<Transaction> findTransactionsBySourceEvidenceId(String evidenceId);

  Transaction findTransactionByEvidenceAndId(String evidenceId, String transactionId);

  int countTransactionsBySourceEvidence(String evidenceId);
}
