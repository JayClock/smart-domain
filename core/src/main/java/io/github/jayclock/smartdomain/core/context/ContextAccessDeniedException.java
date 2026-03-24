package io.github.jayclock.smartdomain.core.context;

public class ContextAccessDeniedException extends IllegalStateException {
  public ContextAccessDeniedException(String message) {
    super(message);
  }
}
