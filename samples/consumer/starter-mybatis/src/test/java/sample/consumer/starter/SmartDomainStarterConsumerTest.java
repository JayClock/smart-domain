package sample.consumer.starter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jayclock.smartdomain.boot.EnableSmartDomainMybatis;
import io.github.jayclock.smartdomain.core.Entity;
import io.github.jayclock.smartdomain.core.HasMany;
import io.github.jayclock.smartdomain.mybatis.AssociationMapping;
import io.github.jayclock.smartdomain.mybatis.GenericEntityHydrator;
import io.github.jayclock.smartdomain.mybatis.database.EntityList;
import javax.sql.DataSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class SmartDomainStarterConsumerTest {

  @Test
  void should_bootstrap_starter_using_published_coordinates_only() throws Exception {
    try (AnnotationConfigApplicationContext context =
        new AnnotationConfigApplicationContext(StarterConsumerConfiguration.class)) {
      GenericEntityHydrator hydrator = context.getBean(GenericEntityHydrator.class);
      CacheManager cacheManager = context.getBean(CacheManager.class);
      PingMapper pingMapper = context.getBean(PingMapper.class);

      assertTrue(
          hydrator.isEntity(
              new Account(
                  "CASH-001", new AccountDescription("CNY 0.00"), new AccountTransactions())));
      assertTrue(
          hydrator.isEntity(new Transaction("TX-001", new TransactionDescription("CNY 100.00"))));
      assertEquals(1, pingMapper.ping());
      assertEquals("HydratingCacheManager", cacheManager.getClass().getSimpleName());
      assertTrue(context.containsBean("sqlSessionFactory"));
    }
  }

  @Configuration
  @MapperScan(basePackageClasses = PingMapper.class)
  @EnableSmartDomainMybatis(
      associationBasePackages = "sample.consumer.starter",
      leafEntityTypes = {Transaction.class})
  static class StarterConsumerConfiguration {

    @Bean
    DataSource dataSource() {
      DriverManagerDataSource dataSource = new DriverManagerDataSource();
      dataSource.setDriverClassName("org.h2.Driver");
      dataSource.setUrl("jdbc:h2:mem:smart-domain-consumer;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
      dataSource.setUsername("sa");
      dataSource.setPassword("");
      return dataSource;
    }

    @Bean
    SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
      SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
      factoryBean.setDataSource(dataSource);
      return factoryBean.getObject();
    }
  }

  @Mapper
  interface PingMapper {
    @Select("select 1")
    int ping();
  }

  static final class Account implements Entity<String, AccountDescription> {
    private final String identity;
    private final AccountDescription description;
    private final Transactions transactions;

    Account(String identity, AccountDescription description, Transactions transactions) {
      this.identity = identity;
      this.description = description;
      this.transactions = transactions;
    }

    @Override
    public String getIdentity() {
      return identity;
    }

    @Override
    public AccountDescription getDescription() {
      return description;
    }

    public HasMany<String, Transaction> transactions() {
      return transactions;
    }

    interface Transactions extends HasMany<String, Transaction> {}
  }

  static final class Transaction implements Entity<String, TransactionDescription> {
    private final String identity;
    private final TransactionDescription description;

    Transaction(String identity, TransactionDescription description) {
      this.identity = identity;
      this.description = description;
    }

    @Override
    public String getIdentity() {
      return identity;
    }

    @Override
    public TransactionDescription getDescription() {
      return description;
    }
  }

  record AccountDescription(String current) {}

  record TransactionDescription(String amount) {}

  @AssociationMapping(entity = Account.class, field = "transactions", parentIdField = "accountId")
  static final class AccountTransactions extends EntityList<String, Transaction>
      implements Account.Transactions {
    private String accountId;

    @Override
    public int size() {
      return 0;
    }

    @Override
    protected java.util.List<Transaction> findEntities(int from, int to) {
      return java.util.List.of();
    }

    @Override
    protected Transaction findEntity(String id) {
      return null;
    }
  }
}
