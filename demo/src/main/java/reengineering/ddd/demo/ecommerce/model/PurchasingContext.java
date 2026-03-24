package reengineering.ddd.demo.ecommerce.model;

import io.github.jayclock.smartdomain.core.context.ContextSwitcher;

public interface PurchasingContext extends ContextSwitcher<User, BuyerAccount, Buyer> {}
