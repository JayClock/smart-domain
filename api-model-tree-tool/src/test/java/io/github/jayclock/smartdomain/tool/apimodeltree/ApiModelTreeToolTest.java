package io.github.jayclock.smartdomain.tool.apimodeltree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jayclock.smartdomain.tool.apimodeltree.classfixtures.ClassEntryUserModel;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class ApiModelTreeToolTest {
  private final ApiModelTreeTool tool = new ApiModelTreeTool();
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void should_build_recursive_navigation_tree_from_java_api_model_file() throws Exception {
    ApiModelNode root = tool.analyze(fixture("UserModel.java"));

    assertEquals("self", root.rel());
    assertEquals("/users/1", root.api());
    assertEquals("/users/1", findLink(root, "update-user").api());

    ApiModelNode accounts = findLink(root, "accounts");
    assertEquals("/users/1/accounts", accounts.api());
    assertEquals("/accounts/1", findLink(accounts, "update-account").api());

    ApiModelNode defaultProject = findLink(root, "default-project");
    assertEquals("/projects/1", defaultProject.api());
    assertEquals("/projects/1", findLink(defaultProject, "delete-project").api());
    assertEquals("/projects/1/agents", findLink(defaultProject, "agents").api());

    ApiModelNode agent = findLink(defaultProject, "agents");
    assertEquals("/agents/1", findLink(agent, "update-agent-status").api());
    assertEquals("/agents/1", findLink(agent, "delete-agent").api());
    assertEquals("/projects/1", findLink(agent, "project").api());
    assertEquals(null, findLink(agent, "project").cycle());
    assertTrue(findLink(agent, "project").links().isEmpty());
  }

  @Test
  void should_build_recursive_navigation_tree_from_halforms_conversation_fixture()
      throws Exception {
    ApiModelNode root = tool.analyze(fixture("ConversationModel.java"));

    assertEquals("/projects/1/conversations/2", root.api());
    assertEquals("/projects/1/conversations/2/messages/stream", findLink(root, "chat").api());
    assertEquals("/projects/1/conversations/2", findLink(root, "delete-conversation").api());
    assertEquals("/projects/1/conversations/2/messages", findLink(root, "messages").api());
    assertEquals(
        "/projects/1/conversations/2/messages",
        findLink(findLink(root, "messages"), "create-message").api());
  }

  @Test
  void should_build_recursive_navigation_tree_from_halforms_diagram_fixture() throws Exception {
    ApiModelNode root = tool.analyze(fixture("DiagramModel.java"));

    assertEquals("/projects/1/diagrams/3", root.api());
    assertEquals("/projects/1", findLink(root, "project").api());
    assertEquals("/projects/1/diagrams", findLink(root, "collection").api());
    assertEquals("/projects/1/diagrams/3", findLink(root, "delete-diagram").api());
    assertEquals("/projects/1/diagrams/3/nodes", findLink(root, "nodes").api());
    assertEquals("/projects/1/diagrams/3/nodes", findLink(root, "create-node").api());
    assertEquals(
        "/projects/1/diagrams/3/nodes/5", findLink(findLink(root, "nodes"), "delete-node").api());
    assertEquals(
        "/projects/1/logical-entities/7",
        findLink(findLink(root, "nodes"), "logical-entity").api());
    assertEquals(
        "/projects/1/logical-entities/7",
        findLink(findLink(findLink(root, "nodes"), "logical-entity"), "delete-logical-entity")
            .api());
  }

  @Test
  void should_analyze_model_class_entrypoint() throws Exception {
    ApiModelNode root = tool.analyze(ClassEntryUserModel.class);

    assertEquals("/class-users/1", root.api());
    assertEquals("/class-users/1", findLink(root, "update-user").api());
    assertEquals("/class-users/1/projects", findLink(root, "projects").api());
    assertEquals("/class-projects/7", findLink(findLink(root, "projects"), "delete-project").api());
    assertEquals(
        "/class-projects/7/conversations",
        findLink(findLink(root, "projects"), "conversations").api());
    assertEquals(
        "/class-projects/7/conversations/3/messages",
        findLink(findLink(findLink(root, "projects"), "conversations"), "messages").api());

    String json = tool.analyzeAsJson(ClassEntryUserModel.class);
    assertTrue(json.contains("\"api\" : \"/class-users/1\""));
    assertTrue(json.contains("\"rel\" : \"update-user\""));
  }

  @Test
  void should_accept_fully_qualified_model_class_in_cli() throws Exception {
    PrintStream originalOut = System.out;
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try {
      System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
      ApiModelTreeCli.main(
          new String[] {
            "io.github.jayclock.smartdomain.tool.apimodeltree.classfixtures.ClassEntryUserModel"
          });
    } finally {
      System.setOut(originalOut);
    }

    String json = output.toString(StandardCharsets.UTF_8);
    assertTrue(json.contains("\"api\" : \"/class-users/1\""));
    assertTrue(json.contains("\"rel\" : \"projects\""));
  }

  @Test
  void should_expose_short_facade_entrypoint() {
    ApiModelNode root = SmartDomainTools.apiModelTree(ClassEntryUserModel.class);

    assertEquals("/class-users/1", root.api());
    assertEquals("/class-users/1/projects", findLink(root, "projects").api());

    String json = SmartDomainTools.apiModelTreeAsJson(ClassEntryUserModel.class);
    assertTrue(json.contains("\"rel\" : \"update-user\""));
  }

  @Test
  void should_optionally_mark_cycle_nodes() throws Exception {
    ApiModelNode root = tool.analyze(fixture("UserModel.java"), new ApiModelTreeOptions(true));

    ApiModelNode project =
        findLink(findLink(findLink(root, "default-project"), "agents"), "project");
    assertEquals("/projects/1", project.api());
    assertEquals(Boolean.TRUE, project.cycle());
    assertTrue(project.links().isEmpty());
  }

  @Test
  void should_render_pretty_json_output() throws Exception {
    String json = tool.analyzeAsJson(fixture("UserModel.java"));

    assertTrue(json.contains("\"rel\" : \"self\""));
    assertTrue(json.contains("\"api\" : \"/projects/1\""));
    assertTrue(json.contains("\"rel\" : \"agents\""));
  }

  @Test
  void should_match_expected_json_tree_for_fixture_user_model() throws Exception {
    JsonNode actual = objectMapper.readTree(tool.analyzeAsJson(fixture("UserModel.java")));
    JsonNode expected =
        objectMapper.readTree(
            """
            {
              "rel": "self",
              "api": "/users/1",
              "links": [
                {
                  "rel": "update-user",
                  "api": "/users/1",
                  "links": []
                },
                {
                  "rel": "accounts",
                  "api": "/users/1/accounts",
                  "links": [
                    {
                      "rel": "update-account",
                      "api": "/accounts/1",
                      "links": []
                    }
                  ]
                },
                {
                  "rel": "default-project",
                  "api": "/projects/1",
                  "links": [
                    {
                      "rel": "delete-project",
                      "api": "/projects/1",
                      "links": []
                    },
                    {
                      "rel": "agents",
                      "api": "/projects/1/agents",
                      "links": [
                        {
                          "rel": "delete-agent",
                          "api": "/agents/1",
                          "links": []
                        },
                        {
                          "rel": "update-agent-status",
                          "api": "/agents/1",
                          "links": []
                        },
                        {
                          "rel": "project",
                          "api": "/projects/1",
                          "links": []
                        }
                      ]
                    }
                  ]
                },
                {
                  "rel": "projects",
                  "api": "/users/1/projects",
                  "links": [
                    {
                      "rel": "delete-project",
                      "api": "/projects/1",
                      "links": []
                    },
                    {
                      "rel": "agents",
                      "api": "/projects/1/agents",
                      "links": [
                        {
                          "rel": "delete-agent",
                          "api": "/agents/1",
                          "links": []
                        },
                        {
                          "rel": "update-agent-status",
                          "api": "/agents/1",
                          "links": []
                        },
                        {
                          "rel": "project",
                          "api": "/projects/1",
                          "links": []
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """);

    assertEquals(expected, actual);
  }

  @Test
  void should_extract_rest_templates_from_real_user_model() throws Exception {
    Path realUserModel = realModel("UserModel.java");
    Assumptions.assumeTrue(
        Files.exists(realUserModel), "Real Team AI UserModel.java not available");
    ApiModelNode root = tool.analyze(realUserModel);

    assertEquals("/users/{id}", root.api());
    assertEquals("/users/{id}", findLink(root, "update-user").api());
    assertEquals("/users/{id}/accounts", findLink(root, "accounts").api());
    assertEquals("/users/{id}/projects", findLink(root, "projects").api());
    assertEquals("/projects/{projectId}", findLink(root, "default-project").api());
    assertEquals(
        "/projects/{projectId}/conversations",
        findLink(findLink(root, "projects"), "create-conversation").api());
    assertEquals(
        "/projects/{projectId}/agents",
        findLink(findLink(root, "default-project"), "agents").api());
    assertEquals(
        "/projects/{projectId}/conversations/{conversation-id}/messages",
        findLink(findLink(findLink(root, "projects"), "conversations"), "messages").api());
    assertEquals(
        "/projects/{projectId}/diagrams/{id}/nodes",
        findLink(findLink(findLink(root, "projects"), "diagrams"), "nodes").api());
    assertEquals(
        "/projects/{projectId}/logical-entities/{id}",
        findLink(
                findLink(findLink(findLink(root, "projects"), "diagrams"), "nodes"),
                "logical-entity")
            .api());
    assertEquals(
        "/projects/{projectId}/mcp-servers/{server-id}",
        findLink(findLink(findLink(root, "projects"), "mcp-servers"), "delete-mcp-server").api());
  }

  @Test
  void should_extract_rest_templates_from_real_root_model() throws Exception {
    Path realRootModel = realModel("RootModel.java");
    Assumptions.assumeTrue(
        Files.exists(realRootModel), "Real Team AI RootModel.java not available");
    ApiModelNode root = tool.analyze(realRootModel);

    assertEquals("/", root.api());
    assertEquals("/auth/login", findLink(root, "login").api());
    assertEquals("/auth/register", findLink(root, "register").api());
    assertEquals("/users/{id}", findLink(root, "me").api());
    assertEquals("/oauth2/authorization/github", findLink(root, "login-oauth-github").api());
  }

  private ApiModelNode findLink(ApiModelNode node, String rel) {
    return StreamSupport.stream(node.links().spliterator(), false)
        .filter(link -> rel.equals(link.rel()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Link not found: " + rel));
  }

  private Path fixture(String name) throws URISyntaxException {
    return Path.of(ApiModelTreeToolTest.class.getResource("/fixtures/" + name).toURI())
        .toAbsolutePath()
        .normalize();
  }

  private Path realModel(String name) {
    return Path.of(
            "../../libs/backend/api/src/main/java/reengineering/ddd/teamai/api/representation/"
                + name)
        .toAbsolutePath()
        .normalize();
  }
}
