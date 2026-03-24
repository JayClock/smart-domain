package io.github.jayclock.smartdomain.api.hateoas.media;

public final class VendorMediaTypes {
  private final String vendorPrefix;

  private VendorMediaTypes(String vendorPrefix) {
    this.vendorPrefix = vendorPrefix;
  }

  public static VendorMediaTypes forVendor(String vendorName) {
    return new VendorMediaTypes("application/vnd." + vendorName);
  }

  public String resource(String resourceName) {
    return vendorPrefix + "." + resourceName + "+json";
  }
}
