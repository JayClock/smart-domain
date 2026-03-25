package io.github.jayclock.smartdomain.tool.apimodeltree;

public record ApiModelTreeOptions(boolean includeCycleMarkers) {
  public static ApiModelTreeOptions defaults() {
    return new ApiModelTreeOptions(false);
  }
}
