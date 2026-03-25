package sample.smartdomain.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiConsumerSampleTest {
  @LocalServerPort private int port;
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldExposeVendorMediaTypeAndHalFormsTemplate() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(List.of(MediaType.parseMediaType("application/prs.hal-forms+json")));

    ResponseEntity<String> response =
        restTemplate.exchange(
            "http://localhost:" + port + "/api/sales-settlements",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class);

    assertThat(response.getStatusCodeValue()).describedAs(response.getBody()).isEqualTo(200);
    assertThat(response.getHeaders().getContentType().toString())
        .startsWith(AccountingMediaTypes.SALES_SETTLEMENT_COLLECTION);

    JsonNode body = objectMapper.readTree(response.getBody());
    JsonNode template = body.at("/_templates/default/properties");

    assertThat(template.isArray()).isTrue();
    assertThat(findProperty(template, "accountId").path("options").path("inline").isArray())
        .isTrue();
    assertThat(findProperty(template, "breakdown").path("_schema").path("type").asText())
        .isEqualTo("object");
  }

  private JsonNode findProperty(JsonNode properties, String name) {
    for (JsonNode property : properties) {
      if (name.equals(property.path("name").asText())) {
        return property;
      }
    }
    throw new AssertionError("Property not found: " + name);
  }
}
