package io.github.jayclock.smartdomain.core.context;

import java.util.Optional;

@FunctionalInterface
public interface ContextSwitcher<Actor, Context, Role extends ContextRole<Actor, Context>> {
  Optional<Role> switchTo(Actor actor, Context context);

  default boolean canSwitchTo(Actor actor, Context context) {
    return switchTo(actor, context).isPresent();
  }

  default Role require(Actor actor, Context context) {
    return switchTo(actor, context)
        .orElseThrow(
            () ->
                new ContextAccessDeniedException(
                    "Actor " + actor + " cannot switch into context " + context));
  }

  default Role require(Actor actor, Context context, String message) {
    return switchTo(actor, context).orElseThrow(() -> new ContextAccessDeniedException(message));
  }

  static <Actor, Context, Role extends ContextRole<Actor, Context>>
      ContextSwitcher<Actor, Context, Role> using(
          ContextRoleResolver<Actor, Context, Role> resolver) {
    return resolver::resolve;
  }
}
