package io.github.jayclock.smartdomain.tool.apimodeltree;

public final class ApiModelTreeCli {
  private ApiModelTreeCli() {}

  public static void main(String[] args) {
    if (args.length < 1 || args.length > 2) {
      usageAndExit();
    }

    try {
      boolean includeCycle = args.length == 2 && "--include-cycle".equals(args[0]);
      String className = includeCycle ? args[1] : args[0];
      if (args.length == 2 && !includeCycle) {
        usageAndExit();
      }

      Class<?> modelClass = Class.forName(className);
      System.out.println(
          SmartDomainTools.apiModelTreeAsJson(modelClass, new ApiModelTreeOptions(includeCycle)));
    } catch (Exception exception) {
      exception.printStackTrace(System.err);
      System.exit(1);
    }
  }

  private static void usageAndExit() {
    System.err.println("Usage: ApiModelTreeCli [--include-cycle] <fully-qualified-model-class>");
    System.exit(1);
  }
}
