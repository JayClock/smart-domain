package io.github.jayclock.smartdomain.boot;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("smart-domain.api")
public class SmartDomainApiProperties {
  private List<String> schemaScanPackages = new ArrayList<>();

  public List<String> getSchemaScanPackages() {
    return schemaScanPackages;
  }

  public void setSchemaScanPackages(List<String> schemaScanPackages) {
    this.schemaScanPackages =
        schemaScanPackages != null ? new ArrayList<>(schemaScanPackages) : new ArrayList<>();
  }
}
