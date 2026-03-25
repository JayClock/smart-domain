package sample.smartdomain.api;

import io.github.jayclock.smartdomain.api.hateoas.options.HalFormsOption;
import io.github.jayclock.smartdomain.api.hateoas.options.HalFormsOptionsCustomizer;
import java.util.List;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsConfiguration;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions;
import org.springframework.stereotype.Component;

@Component
public class AccountingOptionsCustomizer implements HalFormsOptionsCustomizer {
  @Override
  public HalFormsConfiguration customize(HalFormsConfiguration config) {
    return config.withOptions(
        SalesSettlementsApi.CreateSalesSettlementRequest.class,
        "accountId",
        metadata ->
            HalFormsOptions.inline(
                    List.of(
                        new HalFormsOption("CASH-001", "Cash Account"),
                        new HalFormsOption("TRANSIT-001", "Transit Account"),
                        new HalFormsOption("CREDIT-001", "Credit Account")))
                .withPromptField("prompt")
                .withValueField("value")
                .withMinItems(1L)
                .withMaxItems(1L));
  }
}
