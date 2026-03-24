package reengineering.ddd.demo.ecommerce.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(
    scanBasePackages = "reengineering.ddd.demo.ecommerce",
    exclude = DataSourceAutoConfiguration.class)
public class EcommerceDemoApplication {
  public static void main(String[] args) {
    SpringApplication.run(EcommerceDemoApplication.class, args);
  }
}
