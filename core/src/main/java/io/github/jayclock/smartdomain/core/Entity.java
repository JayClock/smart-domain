package io.github.jayclock.smartdomain.core;

public interface Entity<Identity, Description> {
  Identity getIdentity();

  Description getDescription();
}
