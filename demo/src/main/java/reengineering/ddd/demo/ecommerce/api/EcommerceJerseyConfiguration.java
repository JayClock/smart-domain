package reengineering.ddd.demo.ecommerce.api;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EcommerceJerseyConfiguration extends ResourceConfig {
  public EcommerceJerseyConfiguration() {
    register(EcommerceApi.class);
  }
}
