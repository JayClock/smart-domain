package io.github.jayclock.smartdomain.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jayclock.smartdomain.api.hateoas.options.HalFormsOptionsCustomizer;
import io.github.jayclock.smartdomain.api.hateoas.schema.JsonSchemaHalFormsCustomizer;
import io.github.jayclock.smartdomain.api.hateoas.schema.JsonSchemaService;
import jakarta.annotation.PostConstruct;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.HalConfiguration;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsConfiguration;
import org.springframework.hateoas.mediatype.hal.forms.Jackson2HalFormsModule;
import org.springframework.hateoas.server.LinkRelationProvider;

@AutoConfiguration
@EnableConfigurationProperties(SmartDomainApiProperties.class)
@EnableHypermediaSupport(type = {HypermediaType.HAL, HypermediaType.HAL_FORMS})
@ConditionalOnClass(ObjectMapper.class)
public class SmartDomainApiAutoConfiguration {
  private final ObjectMapper objectMapper;
  private final LinkRelationProvider linkRelationProvider;
  private final MessageResolver messageResolver;
  private final HalConfiguration halConfiguration;
  private final AutowireCapableBeanFactory beanFactory;

  public SmartDomainApiAutoConfiguration(
      ObjectMapper objectMapper,
      LinkRelationProvider linkRelationProvider,
      MessageResolver messageResolver,
      HalConfiguration halConfiguration,
      AutowireCapableBeanFactory beanFactory) {
    this.objectMapper = objectMapper;
    this.linkRelationProvider = linkRelationProvider;
    this.messageResolver = messageResolver;
    this.halConfiguration = halConfiguration;
    this.beanFactory = beanFactory;
  }

  @PostConstruct
  void configureObjectMapper() {
    objectMapper.registerModule(new Jackson2HalModule());
    objectMapper.registerModule(new Jackson2HalFormsModule());
    objectMapper.setHandlerInstantiator(
        new Jackson2HalModule.HalHandlerInstantiator(
            linkRelationProvider,
            CurieProvider.NONE,
            messageResolver,
            halConfiguration,
            beanFactory));
  }

  @Bean
  @ConditionalOnMissingBean
  JsonSchemaService jsonSchemaService() {
    return new JsonSchemaService();
  }

  @Bean
  @ConditionalOnMissingBean(HalFormsConfiguration.class)
  HalFormsConfiguration halFormsConfiguration(
      ObjectProvider<List<HalFormsOptionsCustomizer>> customizersProvider) {
    HalFormsConfiguration configuration = new HalFormsConfiguration();
    List<HalFormsOptionsCustomizer> customizers = customizersProvider.getIfAvailable();
    if (customizers == null) {
      return configuration;
    }

    for (HalFormsOptionsCustomizer customizer : customizers) {
      configuration = customizer.customize(configuration);
    }
    return configuration;
  }

  @Bean
  @ConditionalOnMissingBean(name = "jsonSchemaHalFormsCustomizer")
  HalFormsOptionsCustomizer jsonSchemaHalFormsCustomizer(
      SmartDomainApiProperties properties, JsonSchemaService schemaService, ObjectMapper mapper) {
    if (properties.getSchemaScanPackages().isEmpty()) {
      return config -> config;
    }
    return new JsonSchemaHalFormsCustomizer(
        schemaService, mapper, properties.getSchemaScanPackages());
  }
}
