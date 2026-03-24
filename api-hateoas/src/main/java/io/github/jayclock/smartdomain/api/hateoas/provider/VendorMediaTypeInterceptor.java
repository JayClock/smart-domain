package io.github.jayclock.smartdomain.api.hateoas.provider;

import io.github.jayclock.smartdomain.api.hateoas.media.VendorMediaType;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;

/** Overrides the response Content-Type with the media type declared by {@link VendorMediaType}. */
@Provider
public class VendorMediaTypeInterceptor implements ContainerResponseFilter {

  @Context private ResourceInfo resourceInfo;

  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext)
      throws IOException {
    VendorMediaType vendorMediaType = findVendorMediaTypeAnnotation();
    if (vendorMediaType != null) {
      responseContext.getHeaders().putSingle("Content-Type", vendorMediaType.value());
    }
  }

  private VendorMediaType findVendorMediaTypeAnnotation() {
    if (resourceInfo == null) {
      return null;
    }
    Method resourceMethod = resourceInfo.getResourceMethod();
    if (resourceMethod == null) {
      return null;
    }
    return resourceMethod.getAnnotation(VendorMediaType.class);
  }
}
