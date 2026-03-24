package io.github.jayclock.smartdomain.api.hateoas.options;

import org.springframework.hateoas.mediatype.hal.forms.HalFormsConfiguration;

@FunctionalInterface
public interface HalFormsOptionsCustomizer {
  HalFormsConfiguration customize(HalFormsConfiguration config);
}
