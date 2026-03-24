package io.github.jayclock.smartdomain.core.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class ContextSwitcherTest {

  @Test
  void should_switch_to_resolved_role() {
    SampleRole ownerRole = new SampleRole("alice", "project-1", "OWNER");
    ContextRoleResolver<String, String, SampleRole> resolver =
        (actor, context) ->
            "alice".equals(actor) && "project-1".equals(context)
                ? Optional.of(ownerRole)
                : Optional.empty();
    ContextSwitcher<String, String, SampleRole> switcher = ContextSwitcher.using(resolver);

    Optional<SampleRole> switched = switcher.switchTo("alice", "project-1");

    assertTrue(switched.isPresent());
    assertSame(ownerRole, switched.orElseThrow());
    assertTrue(switcher.canSwitchTo("alice", "project-1"));
    assertFalse(switcher.canSwitchTo("bob", "project-1"));
  }

  @Test
  void should_throw_when_required_role_is_missing() {
    ContextRoleResolver<String, String, SampleRole> resolver = (actor, context) -> Optional.empty();
    ContextSwitcher<String, String, SampleRole> switcher = ContextSwitcher.using(resolver);

    ContextAccessDeniedException error =
        assertThrows(
            ContextAccessDeniedException.class, () -> switcher.require("bob", "project-2"));

    assertEquals("Actor bob cannot switch into context project-2", error.getMessage());
  }

  @Test
  void should_use_custom_denied_message() {
    ContextRoleResolver<String, String, SampleRole> resolver = (actor, context) -> Optional.empty();
    ContextSwitcher<String, String, SampleRole> switcher = ContextSwitcher.using(resolver);

    ContextAccessDeniedException error =
        assertThrows(
            ContextAccessDeniedException.class,
            () -> switcher.require("bob", "project-2", "Role escalation is not allowed"));

    assertEquals("Role escalation is not allowed", error.getMessage());
  }

  private record SampleRole(String actor, String context, String name)
      implements ContextRole<String, String> {}
}
