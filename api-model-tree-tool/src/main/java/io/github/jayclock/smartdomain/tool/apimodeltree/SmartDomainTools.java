package io.github.jayclock.smartdomain.tool.apimodeltree;

public final class SmartDomainTools {
  private static final ApiModelTreeTool API_MODEL_TREE_TOOL = new ApiModelTreeTool();

  private SmartDomainTools() {}

  public static ApiModelNode apiModelTree(Class<?> modelClass) {
    return API_MODEL_TREE_TOOL.analyze(modelClass);
  }

  public static ApiModelNode apiModelTree(Class<?> modelClass, ApiModelTreeOptions options) {
    return API_MODEL_TREE_TOOL.analyze(modelClass, options);
  }

  public static String apiModelTreeAsJson(Class<?> modelClass) {
    return API_MODEL_TREE_TOOL.analyzeAsJson(modelClass);
  }

  public static String apiModelTreeAsJson(Class<?> modelClass, ApiModelTreeOptions options) {
    return API_MODEL_TREE_TOOL.analyzeAsJson(modelClass, options);
  }
}
