package reengineering.ddd.demo.ecommerce;

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
import reengineering.ddd.demo.ecommerce.api.EcommerceDemoApplication;
import reengineering.ddd.demo.ecommerce.api.EcommerceMediaTypes;

@SpringBootTest(
    classes = EcommerceDemoApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EcommerceApiTest {
  @LocalServerPort private int port;
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void should_expose_entity_resources_without_role_paths() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(List.of(MediaType.parseMediaType("application/hal+json")));

    ResponseEntity<String> rootResponse =
        restTemplate.exchange(
            "http://localhost:" + port + "/api/ecommerce",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class);

    assertThat(rootResponse.getStatusCodeValue()).isEqualTo(200);
    assertThat(rootResponse.getHeaders().getContentType().toString())
        .startsWith(EcommerceMediaTypes.ROOT);

    JsonNode root = objectMapper.readTree(rootResponse.getBody());
    assertThat(root.path("userName").asText()).isEqualTo("Alex");
    assertThat(root.path("_links").has("user")).isTrue();
    assertThat(root.path("_links").has("marketplace")).isFalse();
    assertThat(root.path("_links").has("buyer-account")).isTrue();
    assertThat(root.path("_links").has("seller-store")).isTrue();
  }

  @Test
  void should_expose_agent_tree_for_ai_navigation() throws Exception {
    ResponseEntity<String> response =
        restTemplate.exchange(
            "http://localhost:" + port + "/api/ecommerce/agent-tree",
            HttpMethod.GET,
            new HttpEntity<>(new HttpHeaders()),
            String.class);

    assertThat(response.getStatusCodeValue()).isEqualTo(200);

    JsonNode tree = objectMapper.readTree(response.getBody());
    assertThat(tree.path("rel").asText()).isEqualTo("self");
    assertThat(tree.path("api").asText()).isEqualTo("/api/ecommerce");
    assertThat(findLink(tree, "buyer-account").path("api").asText())
        .isEqualTo("/api/ecommerce/buyer-accounts/1");
    assertThat(findLink(findLink(tree, "buyer-account"), "create-purchase").path("api").asText())
        .isEqualTo("/api/ecommerce/buyer-accounts/1/purchases");
    assertThat(findLink(findLink(tree, "seller-store"), "create-listing").path("api").asText())
        .isEqualTo("/api/ecommerce/seller-stores/1/listings");
  }

  @Test
  void should_create_purchases_and_listings_via_api() throws Exception {
    ResponseEntity<String> rootResponse =
        restTemplate.exchange(
            "http://localhost:" + port + "/api/ecommerce",
            HttpMethod.GET,
            new HttpEntity<>(new HttpHeaders()),
            String.class);
    JsonNode root = objectMapper.readTree(rootResponse.getBody());
    String buyerAccountId = root.path("buyerAccountId").asText();
    String sellerStoreId = root.path("sellerStoreId").asText();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> listingResponse =
        restTemplate.exchange(
            "http://localhost:" + port + "/api/ecommerce/seller-stores/" + sellerStoreId + "/listings",
            HttpMethod.POST,
            new HttpEntity<>(
                """
                {"productName":"Desk Lamp","inventory":5,"unitPrice":129}
                """,
                headers),
            String.class);

    ResponseEntity<String> purchaseResponse =
        restTemplate.exchange(
            "http://localhost:" + port + "/api/ecommerce/buyer-accounts/" + buyerAccountId + "/purchases",
            HttpMethod.POST,
            new HttpEntity<>(
                """
                {"productName":"Desk Lamp","quantity":1}
                """,
                headers),
            String.class);

    ResponseEntity<String> sellerResponse =
        restTemplate.exchange(
            "http://localhost:" + port + "/api/ecommerce/seller-stores/" + sellerStoreId,
            HttpMethod.GET,
            new HttpEntity<>(new HttpHeaders()),
            String.class);

    ResponseEntity<String> buyerResponse =
        restTemplate.exchange(
            "http://localhost:" + port + "/api/ecommerce/buyer-accounts/" + buyerAccountId,
            HttpMethod.GET,
            new HttpEntity<>(new HttpHeaders()),
            String.class);

    assertThat(listingResponse.getStatusCodeValue()).isEqualTo(201);
    assertThat(purchaseResponse.getStatusCodeValue()).isEqualTo(201);

    JsonNode seller = objectMapper.readTree(sellerResponse.getBody());
    JsonNode buyer = objectMapper.readTree(buyerResponse.getBody());

    assertThat(seller.path("listings")).hasSize(2);
    assertThat(buyer.path("purchases")).hasSize(2);
  }

  @Test
  void should_expose_templates_for_agent_actions() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));

    ResponseEntity<String> buyerResponse =
        restTemplate.exchange(
            "http://localhost:" + port + "/api/ecommerce/buyer-accounts/1",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class);

    ResponseEntity<String> sellerResponse =
        restTemplate.exchange(
            "http://localhost:" + port + "/api/ecommerce/seller-stores/1",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class);

    JsonNode buyer = objectMapper.readTree(buyerResponse.getBody());
    JsonNode seller = objectMapper.readTree(sellerResponse.getBody());

    assertThat(buyer.path("_links").has("purchases")).isTrue();
    assertThat(buyer.path("_templates").has("default")).isTrue();
    assertThat(buyer.path("_templates").path("default").path("target").asText())
        .isEqualTo("/api/ecommerce/buyer-accounts/1/purchases");
    assertThat(buyer.path("_templates").path("default").path("properties")).hasSize(2);

    assertThat(seller.path("_links").has("listings")).isTrue();
    assertThat(seller.path("_templates").has("default")).isTrue();
    assertThat(seller.path("_templates").path("default").path("target").asText())
        .isEqualTo("/api/ecommerce/seller-stores/1/listings");
    assertThat(seller.path("_templates").path("default").path("properties")).hasSize(3);
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
