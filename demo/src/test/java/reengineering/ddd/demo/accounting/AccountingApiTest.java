package reengineering.ddd.demo.accounting;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.StreamSupport;
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
import reengineering.ddd.demo.accounting.api.AccountingDemoApplication;
import reengineering.ddd.demo.accounting.api.AccountingMediaTypes;

@SpringBootTest(
    classes = AccountingDemoApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountingApiTest {
  @LocalServerPort private int port;
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void should_expose_root_customer_and_roles() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(List.of(MediaType.parseMediaType("application/hal+json")));

    ResponseEntity<String> response =
        restTemplate.exchange(
            "http://localhost:" + port + "/api/accounting",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class);

    assertThat(response.getStatusCodeValue()).isEqualTo(200);
    assertThat(response.getHeaders().getContentType().toString())
        .startsWith(AccountingMediaTypes.ROOT);

    JsonNode root = objectMapper.readTree(response.getBody());
    assertThat(root.path("operatorName").asText()).isEqualTo("Olivia");
    assertThat(root.path("customerName").asText()).isEqualTo("ACME Retail");
    assertThat(root.path("activeRoles")).hasSize(4);
    assertThat(
            StreamSupport.stream(root.path("activeRoles").spliterator(), false)
                .map(JsonNode::asText))
        .contains("bookkeeper", "auditor", "accountant", "evidence-reviewer");
    assertThat(root.path("_links").has("customer")).isTrue();
  }

  @Test
  void should_expose_agent_tree_for_accounting_navigation() throws Exception {
    ResponseEntity<String> response =
        restTemplate.exchange(
            "http://localhost:" + port + "/api/accounting/agent-tree",
            HttpMethod.GET,
            new HttpEntity<>(new HttpHeaders()),
            String.class);

    assertThat(response.getStatusCodeValue()).isEqualTo(200);

    JsonNode tree = objectMapper.readTree(response.getBody());
    assertThat(tree.path("api").asText()).isEqualTo("/api/accounting");
    assertThat(findLink(tree, "customer").path("api").asText())
        .isEqualTo("/api/accounting/customers/1");
  }

  @Test
  void should_record_sales_settlement_and_update_account() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> createResponse =
        restTemplate.exchange(
            "http://localhost:"
                + port
                + "/api/accounting/customers/1/source-evidences/sales-settlements",
            HttpMethod.POST,
            new HttpEntity<>(
                """
                {
                  "orderId":"ORDER-1002",
                  "accountId":"CASH-001",
                  "detailAmounts":["300.00","200.00"]
                }
                """,
                headers),
            String.class);

    ResponseEntity<String> accountResponse =
        restTemplate.exchange(
            "http://localhost:" + port + "/api/accounting/customers/1/accounts/CASH-001",
            HttpMethod.GET,
            new HttpEntity<>(new HttpHeaders()),
            String.class);

    assertThat(createResponse.getStatusCodeValue()).isEqualTo(201);

    JsonNode account = objectMapper.readTree(accountResponse.getBody());
    assertThat(account.path("transactions")).hasSize(4);
    assertThat(account.path("current").asText()).isEqualTo("1500.00");
  }

  @Test
  void should_expose_template_for_sales_settlement_recording() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));

    ResponseEntity<String> customerResponse =
        restTemplate.exchange(
            "http://localhost:" + port + "/api/accounting/customers/1",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class);

    JsonNode customer = objectMapper.readTree(customerResponse.getBody());
    assertThat(customer.path("_templates").has("default")).isTrue();
    assertThat(customer.path("_templates").path("default").path("target").asText())
        .isEqualTo("/api/accounting/customers/1/source-evidences/sales-settlements");
    assertThat(customer.path("_templates").path("default").path("properties")).hasSize(3);
  }

  private JsonNode findLink(JsonNode node, String rel) {
    for (JsonNode child : node.path("links")) {
      if (rel.equals(child.path("rel").asText())) {
        return child;
      }
    }
    throw new AssertionError("Link not found: " + rel);
  }
}
