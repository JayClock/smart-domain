package sample.smartdomain.api;

import io.github.jayclock.smartdomain.api.hateoas.media.VendorMediaType;
import io.github.jayclock.smartdomain.api.hateoas.schema.WithJsonSchema;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
@Path("books")
public class BooksApi {
  private final BookCatalog bookCatalog;

  public BooksApi(BookCatalog bookCatalog) {
    this.bookCatalog = bookCatalog;
  }

  @GET
  @VendorMediaType(BookMediaTypes.BOOK_COLLECTION)
  public CollectionModel<BookModel> findAll(@Context UriInfo uriInfo) {
    Link self = Link.of(uriInfo.getAbsolutePath().toString()).withSelfRel();
    CollectionModel<BookModel> model =
        CollectionModel.of(
            bookCatalog.findAll().stream()
                .map(book -> BookModel.of(book, bookHref(uriInfo, book.id())))
                .toList(),
            self);
    model.add(
        Affordances.of(self)
            .afford(HttpMethod.POST)
            .withInput(CreateBookRequest.class)
            .andAfford(HttpMethod.POST)
            .withInput(CreateBookRequest.class)
            .withName("create-book")
            .toLink());
    return model;
  }

  @GET
  @Path("{id}")
  @VendorMediaType(BookMediaTypes.BOOK)
  public BookModel findById(@PathParam("id") String id, @Context UriInfo uriInfo) {
    Book book = bookCatalog.findById(id);
    if (book == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return BookModel.of(book, bookHref(uriInfo, book.id()));
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @VendorMediaType(BookMediaTypes.BOOK)
  public Response create(CreateBookRequest request, @Context UriInfo uriInfo) {
    Book created = bookCatalog.create(request.title(), request.genre(), request.metadata());
    return Response.created(uriInfo.getAbsolutePathBuilder().path(created.id()).build())
        .entity(BookModel.of(created, bookHref(uriInfo, created.id())))
        .build();
  }

  private String bookHref(UriInfo uriInfo, String id) {
    return uriInfo.getBaseUriBuilder().path("books").path(id).build().toString();
  }

  public record CreateBookRequest(
      String title, String genre, @WithJsonSchema(BookMetadata.class) BookMetadata metadata) {}
}
