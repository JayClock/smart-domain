package io.github.jayclock.smartdomain.boot;

import io.github.jayclock.smartdomain.api.jersey.VendorMediaTypeInterceptor;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jersey.ResourceConfigCustomizer;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(ResourceConfig.class)
public class SmartDomainApiJerseyAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(name = "smartDomainApiJerseyCustomizer")
  ResourceConfigCustomizer smartDomainApiJerseyCustomizer() {
    return resourceConfig -> {
      resourceConfig.property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, true);
      resourceConfig.register(VendorMediaTypeInterceptor.class);
    };
  }
}
