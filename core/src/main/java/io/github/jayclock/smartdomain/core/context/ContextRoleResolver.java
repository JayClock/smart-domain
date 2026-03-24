package io.github.jayclock.smartdomain.core.context;

import java.util.Optional;

@FunctionalInterface
public interface ContextRoleResolver<Actor, Context, Role extends ContextRole<Actor, Context>> {
  Optional<Role> resolve(Actor actor, Context context);
}
