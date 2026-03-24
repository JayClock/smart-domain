package reengineering.ddd.demo.library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jayclock.smartdomain.mybatis.AssociationMapping;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import reengineering.ddd.demo.library.model.Library;
import reengineering.ddd.demo.library.model.Shelf;
import reengineering.ddd.demo.library.mybatis.LibraryShelves;
import reengineering.ddd.demo.library.mybatis.MybatisLibraries;
import reengineering.ddd.demo.library.mybatis.ShelfBooks;

class LibraryMybatisTemplateTest {

  @Test
  void should_keep_library_shelves_mapping_one_to_one() throws Exception {
    Field field = Library.class.getDeclaredField("shelves");
    AssociationMapping mapping = LibraryShelves.class.getAnnotation(AssociationMapping.class);

    assertEquals("shelves", field.getName());
    assertTrue(Library.Shelves.class.isAssignableFrom(LibraryShelves.class));
    assertEquals(Library.class, mapping.entity());
    assertEquals("shelves", mapping.field());
    assertEquals("libraryId", mapping.parentIdField());
  }

  @Test
  void should_keep_shelf_books_mapping_one_to_one() throws Exception {
    Field field = Shelf.class.getDeclaredField("books");
    AssociationMapping mapping = ShelfBooks.class.getAnnotation(AssociationMapping.class);

    assertEquals("books", field.getName());
    assertTrue(Shelf.Books.class.isAssignableFrom(ShelfBooks.class));
    assertEquals(Shelf.class, mapping.entity());
    assertEquals("books", mapping.field());
    assertEquals("shelfId", mapping.parentIdField());
  }

  @Test
  void should_expose_repository_template_for_root_aggregate() {
    assertTrue(
        reengineering.ddd.demo.library.model.Libraries.class.isAssignableFrom(
            MybatisLibraries.class));
  }
}
