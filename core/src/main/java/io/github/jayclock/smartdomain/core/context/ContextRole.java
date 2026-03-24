package io.github.jayclock.smartdomain.core.context;

/**
 * A role object bound to an actor operating inside a specific context.
 *
 * <p>This is the Smart Domain counterpart of application-specific role wrappers such as owner,
 * editor, reviewer, or approver.
 */
public interface ContextRole<Actor, Context> {
  Actor actor();

  Context context();
}
