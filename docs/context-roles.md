# Context Roles

Smart Domain supports context-specific role switching in `smart-domain-core`.

Use it when an actor enters a business context and gains a role object with behavior that only
exists inside that context.

## Core Types

- `ContextRole<Actor, Context>`
- `ContextRoleResolver<Actor, Context, Role>`
- `ContextSwitcher<Actor, Context, Role>`
- `ContextAccessDeniedException`

## Recommended Shape

Use a switcher to resolve an actor into a role object for a concrete context:

```java
public interface ProjectParticipant extends ContextRole<User, Project> {}

public interface ProjectContext extends ContextSwitcher<User, Project, ProjectParticipant> {}
```

Then keep behavior on the role object:

```java
public final class ProjectOwner implements ProjectParticipant {
  @Override
  public User actor() {
    return user;
  }

  @Override
  public Project context() {
    return project;
  }

  public void deleteProject() {
    projects.delete(project.getIdentity());
  }
}
```

## Why It Exists

- It replaces ad hoc `if (role == ...)` checks with explicit role objects.
- It keeps context-aware behavior near the domain instead of pushing it into services.
- It gives external products a reusable abstraction instead of forcing application-specific naming.

## Team AI Mapping

Team AI now treats:

- `ProjectContext` as a `ContextSwitcher<User, Project, ProjectParticipant>`
- `ProjectParticipant` as a `ContextRole<User, Project>`
- `ProjectOwner`, `ProjectEditor`, and `ProjectViewer` as concrete context roles
