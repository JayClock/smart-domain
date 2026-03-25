package reengineering.ddd.demo.accounting.model;

import io.github.jayclock.smartdomain.core.context.ContextSwitcher;

public interface BookkeepingContext extends ContextSwitcher<Operator, Customer, Bookkeeper> {}
