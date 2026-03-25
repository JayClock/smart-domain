package reengineering.ddd.demo.accounting.mybatis.config;

import io.github.jayclock.smartdomain.boot.EnableSmartDomainMybatis;
import org.springframework.context.annotation.Configuration;
import reengineering.ddd.demo.accounting.model.Transaction;

@Configuration
@EnableSmartDomainMybatis(
    associationBasePackages = "reengineering.ddd.demo.accounting.mybatis",
    leafEntityTypes = {Transaction.class})
public class AccountingDemoSmartDomainMybatisConfiguration {}
