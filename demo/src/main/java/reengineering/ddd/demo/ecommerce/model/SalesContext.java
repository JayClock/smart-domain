package reengineering.ddd.demo.ecommerce.model;

import io.github.jayclock.smartdomain.core.context.ContextSwitcher;

public interface SalesContext extends ContextSwitcher<User, SellerStore, Seller> {}
