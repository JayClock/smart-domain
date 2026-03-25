package reengineering.ddd.demo.accounting.model;

import io.github.jayclock.smartdomain.core.context.ContextSwitcher;

public interface AuditContext extends ContextSwitcher<Operator, Customer, Auditor> {}
