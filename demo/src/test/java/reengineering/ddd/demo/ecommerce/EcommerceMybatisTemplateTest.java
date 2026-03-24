package reengineering.ddd.demo.ecommerce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jayclock.smartdomain.mybatis.AssociationMapping;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import reengineering.ddd.demo.ecommerce.model.SellerStore;
import reengineering.ddd.demo.ecommerce.model.SellerStores;
import reengineering.ddd.demo.ecommerce.mybatis.MybatisSellerStores;
import reengineering.ddd.demo.ecommerce.mybatis.SellerListings;

class EcommerceMybatisTemplateTest {

  @Test
  void should_keep_seller_store_listings_mapping_one_to_one() throws Exception {
    Field field = SellerStore.class.getDeclaredField("listings");
    AssociationMapping mapping = SellerListings.class.getAnnotation(AssociationMapping.class);

    assertEquals("listings", field.getName());
    assertTrue(SellerStore.Listings.class.isAssignableFrom(SellerListings.class));
    assertEquals(SellerStore.class, mapping.entity());
    assertEquals("listings", mapping.field());
    assertEquals("sellerStoreId", mapping.parentIdField());
  }

  @Test
  void should_expose_repository_template_for_sales_context() {
    assertTrue(SellerStores.class.isAssignableFrom(MybatisSellerStores.class));
  }
}
