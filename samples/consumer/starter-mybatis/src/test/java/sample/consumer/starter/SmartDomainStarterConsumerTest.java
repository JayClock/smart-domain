package sample.consumer.starter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jayclock.smartdomain.boot.EnableSmartDomainMybatis;
import io.github.jayclock.smartdomain.core.Entity;
import io.github.jayclock.smartdomain.core.HasMany;
import io.github.jayclock.smartdomain.mybatis.AssociationMapping;
import io.github.jayclock.smartdomain.mybatis.GenericEntityHydrator;
import io.github.jayclock.smartdomain.mybatis.database.EntityList;
import javax.sql.DataSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class SmartDomainStarterConsumerTest {

  @Test
  void should_bootstrap_starter_using_published_coordinates_only() throws Exception {
    try (AnnotationConfigApplicationContext context =
        new AnnotationConfigApplicationContext(StarterConsumerConfiguration.class)) {
      GenericEntityHydrator hydrator = context.getBean(GenericEntityHydrator.class);
      CacheManager cacheManager = context.getBean(CacheManager.class);
      PingMapper pingMapper = context.getBean(PingMapper.class);

      assertTrue(hydrator.isEntity(new Library("library-1", new LibraryDescription("Central"), new LibraryShelves())));
      assertTrue(hydrator.isEntity(new Shelf("shelf-1", new ShelfDescription("Sci-Fi"))));
      assertEquals(1, pingMapper.ping());
      assertEquals("HydratingCacheManager", cacheManager.getClass().getSimpleName());
      assertTrue(context.containsBean("sqlSessionFactory"));
    }
  }

  @Configuration
  @MapperScan(basePackageClasses = PingMapper.class)
  @EnableSmartDomainMybatis(
      associationBasePackages = "sample.consumer.starter",
      leafEntityTypes = {Shelf.class})
  static class StarterConsumerConfiguration {

    @Bean
    DataSource dataSource() {
      DriverManagerDataSource dataSource = new DriverManagerDataSource();
      dataSource.setDriverClassName("org.h2.Driver");
      dataSource.setUrl("jdbc:h2:mem:smart-domain-consumer;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
      dataSource.setUsername("sa");
      dataSource.setPassword("");
      return dataSource;
    }

    @Bean
    SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
      SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
      factoryBean.setDataSource(dataSource);
      return factoryBean.getObject();
    }
  }

  @Mapper
  interface PingMapper {
    @Select("select 1")
    int ping();
  }

  static final class Library implements Entity<String, LibraryDescription> {
    private final String identity;
    private final LibraryDescription description;
    private final Shelves shelves;

    Library(String identity, LibraryDescription description, Shelves shelves) {
      this.identity = identity;
      this.description = description;
      this.shelves = shelves;
    }

    @Override
    public String getIdentity() {
      return identity;
    }

    @Override
    public LibraryDescription getDescription() {
      return description;
    }

    public HasMany<String, Shelf> shelves() {
      return shelves;
    }

    interface Shelves extends HasMany<String, Shelf> {}
  }

  static final class Shelf implements Entity<String, ShelfDescription> {
    private final String identity;
    private final ShelfDescription description;

    Shelf(String identity, ShelfDescription description) {
      this.identity = identity;
      this.description = description;
    }

    @Override
    public String getIdentity() {
      return identity;
    }

    @Override
    public ShelfDescription getDescription() {
      return description;
    }
  }

  record LibraryDescription(String name) {}

  record ShelfDescription(String name) {}

  @AssociationMapping(entity = Library.class, field = "shelves", parentIdField = "libraryId")
  static final class LibraryShelves extends EntityList<String, Shelf> implements Library.Shelves {
    private int libraryId;

    @Override
    public int size() {
      return 0;
    }

    @Override
    protected java.util.List<Shelf> findEntities(int from, int to) {
      return java.util.List.of();
    }

    @Override
    protected Shelf findEntity(String id) {
      return null;
    }
  }
}
