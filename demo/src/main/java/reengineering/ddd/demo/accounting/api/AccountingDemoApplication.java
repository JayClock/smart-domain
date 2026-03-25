package reengineering.ddd.demo.accounting.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(
    scanBasePackages = "reengineering.ddd.demo.accounting",
    exclude = DataSourceAutoConfiguration.class)
public class AccountingDemoApplication {
  public static void main(String[] args) {
    SpringApplication.run(AccountingDemoApplication.class, args);
  }
}
