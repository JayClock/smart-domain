package reengineering.ddd.demo.accounting.model;

import io.github.jayclock.smartdomain.core.context.ContextSwitcher;

public interface AccountContext extends ContextSwitcher<Operator, Account, Accountant> {}
