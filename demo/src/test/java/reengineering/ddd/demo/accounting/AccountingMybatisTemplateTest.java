package reengineering.ddd.demo.accounting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jayclock.smartdomain.mybatis.AssociationMapping;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import reengineering.ddd.demo.accounting.model.Account;
import reengineering.ddd.demo.accounting.mybatis.AccountTransactions;

class AccountingMybatisTemplateTest {

  @Test
  void should_keep_account_transactions_mapping_one_to_one() throws Exception {
    Field field = Account.class.getDeclaredField("transactions");
    AssociationMapping mapping = AccountTransactions.class.getAnnotation(AssociationMapping.class);

    assertEquals("transactions", field.getName());
    assertTrue(Account.Transactions.class.isAssignableFrom(AccountTransactions.class));
    assertEquals(Account.class, mapping.entity());
    assertEquals("transactions", mapping.field());
    assertEquals("accountId", mapping.parentIdField());
  }
}
