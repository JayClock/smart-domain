package reengineering.ddd.demo.accounting.api;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountingJerseyConfiguration extends ResourceConfig {
  public AccountingJerseyConfiguration() {
    register(AccountingApi.class);
  }
}
