package sample.smartdomain.api;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountingJerseyConfiguration extends ResourceConfig {
  public AccountingJerseyConfiguration() {
    register(SalesSettlementsApi.class);
  }
}
