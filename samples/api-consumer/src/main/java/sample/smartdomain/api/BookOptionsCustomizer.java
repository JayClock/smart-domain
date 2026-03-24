package sample.smartdomain.api;

import io.github.jayclock.smartdomain.api.hateoas.options.HalFormsOption;
import io.github.jayclock.smartdomain.api.hateoas.options.HalFormsOptionsCustomizer;
import java.util.List;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsConfiguration;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions;
import org.springframework.stereotype.Component;

@Component
public class BookOptionsCustomizer implements HalFormsOptionsCustomizer {
  @Override
  public HalFormsConfiguration customize(HalFormsConfiguration config) {
    return config.withOptions(
        BooksApi.CreateBookRequest.class,
        "genre",
        metadata ->
            HalFormsOptions.inline(
                    List.of(
                        new HalFormsOption("reference", "Reference"),
                        new HalFormsOption("playbook", "Playbook"),
                        new HalFormsOption("case-study", "Case Study")))
                .withPromptField("prompt")
                .withValueField("value")
                .withMinItems(1L)
                .withMaxItems(1L));
  }
}
