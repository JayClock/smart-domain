package sample.smartdomain.api;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BooksJerseyConfiguration extends ResourceConfig {
  public BooksJerseyConfiguration() {
    register(BooksApi.class);
  }
}
