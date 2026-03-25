package io.github.jayclock.smartdomain.tool.apimodeltree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

class ApiModelTreeTool {
  private final ObjectMapper objectMapper;

  public ApiModelTreeTool() {
    this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    StaticJavaParser.getParserConfiguration()
        .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
  }

  public ApiModelNode analyze(Class<?> modelClass) {
    return analyze(modelClass, ApiModelTreeOptions.defaults());
  }

  public ApiModelNode analyze(Class<?> modelClass, ApiModelTreeOptions options) {
    return analyze(resolveSourceFile(modelClass), options);
  }

  ApiModelNode analyze(Path entryModelFile) {
    return analyze(entryModelFile, ApiModelTreeOptions.defaults());
  }

  ApiModelNode analyze(Path entryModelFile, ApiModelTreeOptions options) {
    try {
      Analyzer analyzer = new Analyzer(options);
      return analyzer.analyze(entryModelFile.toAbsolutePath().normalize());
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to analyze model file: " + entryModelFile, exception);
    }
  }

  public String analyzeAsJson(Class<?> modelClass) {
    return analyzeAsJson(modelClass, ApiModelTreeOptions.defaults());
  }

  public String analyzeAsJson(Class<?> modelClass, ApiModelTreeOptions options) {
    return analyzeAsJson(resolveSourceFile(modelClass), options);
  }

  String analyzeAsJson(Path entryModelFile) {
    return analyzeAsJson(entryModelFile, ApiModelTreeOptions.defaults());
  }

  String analyzeAsJson(Path entryModelFile, ApiModelTreeOptions options) {
    try {
      return objectMapper.writeValueAsString(analyze(entryModelFile, options));
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException(
          "Unable to serialize API model tree for " + entryModelFile, exception);
    }
  }

  private Path resolveSourceFile(Class<?> modelClass) {
    Class<?> topLevelClass = topLevelClass(modelClass);
    String classResourceName = topLevelClass.getName().replace('.', '/') + ".class";
    URL resource =
        topLevelClass.getClassLoader() == null
            ? ClassLoader.getSystemResource(classResourceName)
            : topLevelClass.getClassLoader().getResource(classResourceName);
    if (resource == null) {
      throw new IllegalArgumentException("Unable to locate compiled class for " + topLevelClass.getName());
    }
    if (!"file".equalsIgnoreCase(resource.getProtocol())) {
      throw new IllegalArgumentException(
          "Unable to derive source path for "
              + topLevelClass.getName()
              + " from protocol "
              + resource.getProtocol());
    }

    Path classFile;
    try {
      classFile = Path.of(resource.toURI()).toAbsolutePath().normalize();
    } catch (URISyntaxException exception) {
      throw new IllegalArgumentException(
          "Unable to derive source path for " + topLevelClass.getName(), exception);
    }

    Path sourceFile = compiledClassToSourceFile(classFile, classResourceName, topLevelClass);
    if (!Files.exists(sourceFile)) {
      throw new IllegalArgumentException(
          "Unable to locate source file for "
              + topLevelClass.getName()
              + ". Expected "
              + sourceFile);
    }
    return sourceFile;
  }

  private Path compiledClassToSourceFile(
      Path classFile, String classResourceName, Class<?> topLevelClass) {
    String normalizedClassFile = classFile.toString().replace('\\', '/');
    String resourceSuffix = "/" + classResourceName;
    if (!normalizedClassFile.endsWith(resourceSuffix)) {
      throw new IllegalArgumentException("Unsupported compiled class location: " + classFile);
    }
    Path classesRoot =
        Path.of(normalizedClassFile.substring(0, normalizedClassFile.length() - resourceSuffix.length()));
    if (classesRoot.getFileName() == null) {
      throw new IllegalArgumentException("Unsupported compiled class location: " + classFile);
    }

    String sourceSet = classesRoot.getFileName().toString();
    if (!"main".equals(sourceSet) && !"test".equals(sourceSet)) {
      throw new IllegalArgumentException("Unsupported compiled class location: " + classFile);
    }

    Path javaDir = classesRoot.getParent();
    Path classesDir = javaDir == null ? null : javaDir.getParent();
    Path buildDir = classesDir == null ? null : classesDir.getParent();
    Path projectDir = buildDir == null ? null : buildDir.getParent();
    if (javaDir == null || classesDir == null || buildDir == null || projectDir == null) {
      throw new IllegalArgumentException("Unsupported compiled class location: " + classFile);
    }
    if (!"java".equals(javaDir.getFileName().toString())
        || !"classes".equals(classesDir.getFileName().toString())
        || !"build".equals(buildDir.getFileName().toString())) {
      throw new IllegalArgumentException("Unsupported compiled class location: " + classFile);
    }

    return projectDir
        .resolve("src")
        .resolve(sourceSet)
        .resolve("java")
        .resolve(topLevelClass.getName().replace('.', '/') + ".java")
        .toAbsolutePath()
        .normalize();
  }

  private Class<?> topLevelClass(Class<?> modelClass) {
    Class<?> current = modelClass;
    while (current.getEnclosingClass() != null) {
      current = current.getEnclosingClass();
    }
    return current;
  }

  private static final class Analyzer {
    private final ApiModelTreeOptions options;
    private final Map<Path, ParsedJavaFile> parsedFiles = new HashMap<>();
    private final Map<String, ModelTree> modelTrees = new HashMap<>();
    private final Map<String, Optional<BuilderState>> builderStateCache = new HashMap<>();
    private final Map<String, Optional<Path>> resourceModelCache = new HashMap<>();

    private Analyzer(ApiModelTreeOptions options) {
      this.options = options;
    }

    private ApiModelNode analyze(Path entryModelFile) throws IOException {
      ParsedJavaFile parsed = parse(entryModelFile);
      String className = parsed.primaryClassName();
      ModelTree tree = buildModelTree(entryModelFile, className, new ArrayDeque<>());
      return new ApiModelNode("self", tree.selfApi(), null, tree.links());
    }

    private ModelTree buildModelTree(Path file, String className, Deque<String> stack)
        throws IOException {
      String key = key(file, className);
      ModelTree cached = modelTrees.get(key);
      if (cached != null) {
        return cached;
      }

      ParsedJavaFile parsed = parse(file);
      ParsedModelClass modelClass = parsed.modelClassByName(className);
      if (modelClass == null) {
        throw new IllegalArgumentException("Model class not found: " + className + " in " + file);
      }

      stack.push(key);
      List<ApiModelNode> links = new ArrayList<>();
      for (ParsedLink link : modelClass.links()) {
        if ("self".equals(link.rel())) {
          continue;
        }

        List<ApiModelNode> childLinks = List.of();
        Boolean cycle = null;
        Optional<Path> targetFile =
            Optional.ofNullable(link.targetModelFile())
                .or(() -> resolveTargetFile(modelClass.referencedModelFiles(), link.rel()));
        if (targetFile.isPresent()) {
          Path resolvedFile = targetFile.orElseThrow();
          ParsedJavaFile targetParsed = parse(resolvedFile);
          String targetClassName = targetParsed.primaryClassName();
          String targetKey = key(resolvedFile, targetClassName);
          if (!stack.contains(targetKey)) {
            childLinks = buildModelTree(resolvedFile, targetClassName, stack).links();
          } else if (options.includeCycleMarkers()) {
            cycle = true;
          }
        }

        links.add(new ApiModelNode(link.rel(), link.api(), cycle, childLinks));
      }
      stack.pop();

      String selfApi =
          modelClass.links().stream()
              .filter(link -> "self".equals(link.rel()))
              .map(ParsedLink::api)
              .filter(Objects::nonNull)
              .findFirst()
              .orElse(null);
      ModelTree tree = new ModelTree(selfApi, List.copyOf(links));
      modelTrees.put(key, tree);
      return tree;
    }

    private Optional<Path> resolveTargetFile(Map<String, Path> referencedModelFiles, String rel) {
      if ("self".equals(rel) || referencedModelFiles.isEmpty()) {
        return Optional.empty();
      }

      List<String> relTokens = normalizeRel(rel);
      Path exact = referencedModelFiles.get(joinTokens(relTokens));
      if (exact != null) {
        return Optional.of(exact);
      }

      List<Map.Entry<String, Path>> ranked =
          referencedModelFiles.entrySet().stream()
              .filter(entry -> suffixMatches(relTokens, splitTokens(entry.getKey())))
              .sorted(
                  Comparator.<Map.Entry<String, Path>>comparingInt(
                          entry -> splitTokens(entry.getKey()).size())
                      .thenComparing(Map.Entry::getKey))
              .toList();
      if (!ranked.isEmpty()) {
        return Optional.of(ranked.get(0).getValue());
      }

      return Optional.empty();
    }

    private ParsedJavaFile parse(Path file) throws IOException {
      ParsedJavaFile cached = parsedFiles.get(file);
      if (cached != null) {
        return cached;
      }

      CompilationUnit unit = StaticJavaParser.parse(Files.readString(file));
      Path sourceRoot = resolveSourceRoot(file, unit);
      SourceContext sourceContext = new SourceContext(file, sourceRoot, unit);

      List<ParsedModelClass> modelClasses =
          unit.findAll(ClassOrInterfaceDeclaration.class).stream()
              .filter(ClassOrInterfaceDeclaration::isPublic)
              .map(declaration -> parseModelClass(sourceContext, declaration))
              .filter(Objects::nonNull)
              .toList();

      ParsedJavaFile parsed = new ParsedJavaFile(sourceContext, modelClasses);
      parsedFiles.put(file, parsed);
      return parsed;
    }

    private ParsedModelClass parseModelClass(
        SourceContext sourceContext, ClassOrInterfaceDeclaration declaration) {
      if (declaration.getExtendedTypes().stream()
          .map(ClassOrInterfaceType::getNameAsString)
          .noneMatch(name -> name.startsWith("RepresentationModel"))) {
        return null;
      }

      Set<ParsedLink> links = new LinkedHashSet<>();
      Map<String, Path> referencedModelFiles = new LinkedHashMap<>();
      Map<String, Path> importedModelFiles =
          resolveImportedFilesBySuffix(sourceContext, sourceContext.unit().getImports(), "Model");

      for (MethodCallExpr addCall : declaration.findAll(MethodCallExpr.class)) {
        if (!"add".equals(addCall.getNameAsString()) || addCall.getArguments().isEmpty()) {
          continue;
        }
        links.addAll(extractLinks(sourceContext, addCall, addCall.getArgument(0)));
      }

      Set<String> referencedModels = new LinkedHashSet<>();
      for (ObjectCreationExpr creationExpr : declaration.findAll(ObjectCreationExpr.class)) {
        collectTypeNameBySuffix(referencedModels, creationExpr.getType(), "Model");
      }
      for (ClassOrInterfaceType type : declaration.findAll(ClassOrInterfaceType.class)) {
        collectTypeNameBySuffix(referencedModels, type, "Model");
      }

      for (String modelName : referencedModels) {
        Path imported = importedModelFiles.get(modelName);
        if (imported != null) {
          referencedModelFiles.put(normalizeClassName(stripSuffix(modelName, "Model")), imported);
          continue;
        }

        Path samePackage = resolveSamePackageClass(sourceContext.file(), modelName);
        if (samePackage != null && !samePackage.equals(sourceContext.file())) {
          referencedModelFiles.put(
              normalizeClassName(stripSuffix(modelName, "Model")), samePackage.normalize());
        }
      }

      return new ParsedModelClass(
          declaration.getNameAsString(), List.copyOf(links), referencedModelFiles);
    }

    private List<ParsedLink> extractLinks(
        SourceContext sourceContext, Node context, Expression expression) {
      Optional<String> rel = extractRel(expression);
      if (rel.isEmpty()) {
        return List.of();
      }
      List<String> names = extractNames(expression);
      String relName = names.stream().filter(rel.orElseThrow()::equals).findFirst().orElse(null);

      Optional<Expression> targetExpression = extractLinkTargetExpression(expression);
      String api =
          targetExpression
              .flatMap(target -> resolveBuilderState(sourceContext, context, target, new ArrayDeque<>()))
              .map(BuilderState::path)
              .orElse(null);

      Path targetModelFile =
          targetExpression
              .flatMap(target -> resolveTargetModelFile(sourceContext, context, target))
              .orElse(null);

      List<ParsedLink> parsedLinks = new ArrayList<>();
      parsedLinks.add(new ParsedLink(rel.orElseThrow(), api, relName, targetModelFile));

      for (String name : names) {
        if (name.equals(rel.orElseThrow())) {
          continue;
        }
        parsedLinks.add(new ParsedLink(name, api, name, null));
      }

      return List.copyOf(parsedLinks);
    }

    private Optional<String> extractRel(Expression expression) {
      Optional<String> withRel =
          expression.findAll(MethodCallExpr.class).stream()
              .filter(call -> "withRel".equals(call.getNameAsString()))
              .map(call -> stringArgument(call, 0))
              .flatMap(Optional::stream)
              .findFirst();
      if (withRel.isPresent()) {
        return withRel;
      }

      boolean selfRel =
          expression.findAll(MethodCallExpr.class).stream()
              .anyMatch(call -> "withSelfRel".equals(call.getNameAsString()));
      if (selfRel) {
        return Optional.of("self");
      }

      return expression.findAll(MethodCallExpr.class).stream()
          .filter(call -> "of".equals(call.getNameAsString()))
          .filter(call -> call.getScope().map(this::scopeName).orElse("").endsWith("Link"))
          .map(call -> stringArgument(call, 1))
          .flatMap(Optional::stream)
          .findFirst();
    }

    private Optional<Expression> extractLinkTargetExpression(Expression expression) {
      return expression.findAll(MethodCallExpr.class).stream()
          .filter(call -> "of".equals(call.getNameAsString()))
          .filter(call -> call.getScope().map(this::scopeName).orElse("").endsWith("Link"))
          .filter(call -> !call.getArguments().isEmpty())
          .map(call -> call.getArgument(0))
          .findFirst();
    }

    private List<String> extractNames(Expression expression) {
      return expression.findAll(MethodCallExpr.class).stream()
          .filter(call -> "withName".equals(call.getNameAsString()))
          .map(call -> stringArgument(call, 0))
          .flatMap(Optional::stream)
          .distinct()
          .toList();
    }

    private Optional<Path> resolveTargetModelFile(
        SourceContext sourceContext, Node context, Expression expression) {
      return resolveBuilderState(sourceContext, context, expression, new ArrayDeque<>())
          .flatMap(state -> resolveEndpointTargetModel(state.endpoint()));
    }

    private Optional<Path> resolveEndpointTargetModel(Endpoint endpoint) {
      if (endpoint.modelFile() != null) {
        return Optional.of(endpoint.modelFile());
      }
      if (endpoint.resourceFile() == null) {
        return Optional.empty();
      }

      String cacheKey = endpoint.resourceFile().toString();
      if (resourceModelCache.containsKey(cacheKey)) {
        return resourceModelCache.get(cacheKey);
      }

      try {
        ParsedJavaFile resourceParsed = parse(endpoint.resourceFile());
        Optional<Path> resolved =
            resourceParsed.sourceContext().unit().findAll(MethodDeclaration.class).stream()
                .filter(method -> hasAnnotation(method, "GET"))
                .map(method -> resolveReturnedTypeFile(resourceParsed.sourceContext(), method.getType(), "Model"))
                .flatMap(Optional::stream)
                .findFirst();
        resourceModelCache.put(cacheKey, resolved);
        return resolved;
      } catch (IOException exception) {
        throw new IllegalStateException(
            "Unable to resolve resource return model: " + endpoint.resourceFile(), exception);
      }
    }

    private Optional<BuilderState> resolveBuilderState(
        SourceContext sourceContext, Node context, Expression expression, Deque<String> stack) {
      if (expression instanceof StringLiteralExpr literalExpr) {
        return Optional.of(new BuilderState(normalizePath(literalExpr.getValue()), Endpoint.none()));
      }

      if (expression instanceof EnclosedExpr enclosedExpr) {
        return resolveBuilderState(sourceContext, context, enclosedExpr.getInner(), stack);
      }

      if (expression instanceof NameExpr nameExpr) {
        return resolveVariableInitializer(context, nameExpr.getNameAsString())
            .flatMap(initializer -> resolveBuilderState(sourceContext, context, initializer, stack));
      }

      if (!(expression instanceof MethodCallExpr call)) {
        return Optional.empty();
      }

      String methodName = call.getNameAsString();
      if ("getPath".equals(methodName) || "build".equals(methodName) || "queryParam".equals(methodName)) {
        return call.getScope()
            .flatMap(scope -> resolveBuilderState(sourceContext, context, scope, stack));
      }

      if ("getBaseUriBuilder".equals(methodName) || "getRequestUri".equals(methodName)) {
        return Optional.of(new BuilderState("/", Endpoint.none()));
      }

      if ("path".equals(methodName)) {
        BuilderState base =
            call.getScope()
                .flatMap(scope -> resolveBuilderState(sourceContext, context, scope, stack))
                .orElse(new BuilderState("/", Endpoint.none()));
        String segment = resolvePathSegment(sourceContext, call).orElse("/");
        Endpoint endpoint = resolvePathEndpoint(sourceContext, call).orElse(base.endpoint());
        return Optional.of(new BuilderState(joinPaths(base.path(), segment), endpoint));
      }

      if (call.getScope().isPresent()) {
        String scopeName = scopeName(call.getScope().orElseThrow());
        if (scopeName.endsWith("ApiTemplates")) {
          return resolveUriBuilderMethod(
              resolveClassSourceContext(sourceContext, "ApiTemplates"), methodName, stack);
        }
      } else {
        return resolveUriBuilderMethod(Optional.of(sourceContext), methodName, stack);
      }

      return Optional.empty();
    }

    private Optional<BuilderState> resolveUriBuilderMethod(
        Optional<SourceContext> sourceContext, String methodName, Deque<String> stack) {
      if (sourceContext.isEmpty()) {
        return Optional.empty();
      }

      SourceContext context = sourceContext.orElseThrow();
      String key = context.file() + "#" + methodName;
      if (builderStateCache.containsKey(key)) {
        return builderStateCache.get(key);
      }
      if (stack.contains(key)) {
        return Optional.empty();
      }

      Optional<MethodDeclaration> method =
          context.unit().findAll(MethodDeclaration.class).stream()
              .filter(candidate -> candidate.getNameAsString().equals(methodName))
              .filter(candidate -> candidate.getType().asString().endsWith("UriBuilder"))
              .findFirst();
      if (method.isEmpty()) {
        builderStateCache.put(key, Optional.empty());
        return Optional.empty();
      }

      stack.push(key);
      try {
        Optional<BuilderState> resolved =
            method.get().findFirst(ReturnStmt.class)
                .flatMap(ReturnStmt::getExpression)
                .flatMap(returnExpr -> resolveBuilderState(context, method.get(), returnExpr, stack));
        builderStateCache.put(key, resolved);
        return resolved;
      } finally {
        stack.pop();
      }
    }

    private Optional<String> resolvePathSegment(SourceContext sourceContext, MethodCallExpr call) {
      if (call.getArguments().size() == 1) {
        Expression argument = call.getArgument(0);
        if (argument instanceof StringLiteralExpr literalExpr) {
          return Optional.of(literalExpr.getValue());
        }
        if (argument instanceof ClassExpr classExpr) {
          return resolveClassPath(sourceContext, classExpr.getType().asString());
        }
      }

      if (call.getArguments().size() >= 2
          && call.getArgument(0) instanceof ClassExpr classExpr
          && call.getArgument(1) instanceof StringLiteralExpr literalExpr) {
        return resolveMethodPath(sourceContext, classExpr.getType().asString(), literalExpr.getValue());
      }

      return Optional.empty();
    }

    private Optional<Endpoint> resolvePathEndpoint(
        SourceContext sourceContext, MethodCallExpr call) {
      if (call.getArguments().size() == 1 && call.getArgument(0) instanceof ClassExpr classExpr) {
        return resolveClassFile(sourceContext, classExpr.getType().asString())
            .map(file -> new Endpoint(null, file));
      }

      if (call.getArguments().size() >= 2
          && call.getArgument(0) instanceof ClassExpr classExpr
          && call.getArgument(1) instanceof StringLiteralExpr literalExpr) {
        return resolveEndpointFromMethod(
            sourceContext, classExpr.getType().asString(), literalExpr.getValue());
      }

      return Optional.empty();
    }

    private Optional<Endpoint> resolveEndpointFromMethod(
        SourceContext sourceContext, String className, String methodName) {
      Optional<SourceContext> classContext = resolveClassSourceContext(sourceContext, className);
      if (classContext.isEmpty()) {
        return Optional.empty();
      }

      SourceContext targetClass = classContext.orElseThrow();
      Optional<MethodDeclaration> method =
          targetClass.unit().findAll(MethodDeclaration.class).stream()
              .filter(candidate -> candidate.getNameAsString().equals(methodName))
              .findFirst();
      if (method.isEmpty()) {
        return Optional.empty();
      }

      Optional<Path> modelFile = resolveReturnedTypeFile(targetClass, method.get().getType(), "Model");
      if (modelFile.isPresent()) {
        return Optional.of(new Endpoint(modelFile.orElseThrow(), null));
      }

      Optional<Path> resourceFile = resolveReturnedTypeFile(targetClass, method.get().getType(), "Api");
      if (resourceFile.isPresent()) {
        return Optional.of(new Endpoint(null, resourceFile.orElseThrow()));
      }

      return Optional.empty();
    }

    private Optional<String> resolveClassPath(SourceContext sourceContext, String className) {
      return resolveClassSourceContext(sourceContext, className)
          .flatMap(context -> findClassDeclaration(context, className).flatMap(this::pathAnnotationValue));
    }

    private Optional<String> resolveMethodPath(
        SourceContext sourceContext, String className, String methodName) {
      return resolveClassSourceContext(sourceContext, className)
          .flatMap(
              context ->
                  context.unit().findAll(MethodDeclaration.class).stream()
                      .filter(method -> method.getNameAsString().equals(methodName))
                      .findFirst()
                      .flatMap(this::pathAnnotationValue));
    }

    private Optional<Path> resolveReturnedTypeFile(
        SourceContext sourceContext, Type type, String suffix) {
      if (!type.isClassOrInterfaceType()) {
        return Optional.empty();
      }

      ClassOrInterfaceType classType = type.asClassOrInterfaceType();
      String typeName = classType.getNameAsString();
      if (typeName.endsWith(suffix)) {
        Optional<Path> direct = resolveClassFile(sourceContext, typeName);
        if (direct.isPresent()) {
          return direct;
        }
      }

      if (classType.getTypeArguments().isEmpty()) {
        return Optional.empty();
      }

      return classType.getTypeArguments().orElseThrow().stream()
          .map(argument -> resolveReturnedTypeFile(sourceContext, argument, suffix))
          .flatMap(Optional::stream)
          .findFirst();
    }

    private Optional<Expression> resolveVariableInitializer(Node context, String variableName) {
      Optional<MethodDeclaration> method = context.findAncestor(MethodDeclaration.class);
      if (method.isPresent()) {
        Optional<Expression> initializer = findVariableInitializer(method.orElseThrow(), variableName);
        if (initializer.isPresent()) {
          return initializer;
        }
      }

      Optional<ClassOrInterfaceDeclaration> type = context.findAncestor(ClassOrInterfaceDeclaration.class);
      if (type.isPresent()) {
        return findVariableInitializer(type.orElseThrow(), variableName);
      }

      return Optional.empty();
    }

    private Optional<Expression> findVariableInitializer(Node scope, String variableName) {
      return scope.findAll(VariableDeclarator.class).stream()
          .filter(variable -> variable.getNameAsString().equals(variableName))
          .map(VariableDeclarator::getInitializer)
          .flatMap(Optional::stream)
          .findFirst();
    }

    private Optional<SourceContext> resolveClassSourceContext(
        SourceContext sourceContext, String className) {
      return resolveClassFile(sourceContext, className)
          .map(
              file -> {
                try {
                  return parse(file).sourceContext();
                } catch (IOException exception) {
                  throw new IllegalStateException("Unable to parse class file: " + file, exception);
                }
              });
    }

    private Optional<Path> resolveClassFile(SourceContext sourceContext, String className) {
      for (ImportDeclaration importDeclaration : sourceContext.unit().getImports()) {
        if (importDeclaration.isAsterisk()) {
          continue;
        }
        if (!importDeclaration.getName().getIdentifier().equals(className)) {
          continue;
        }

        Path importedPath =
            sourceContext
                .sourceRoot()
                .resolve(importDeclaration.getNameAsString().replace('.', '/') + ".java")
                .normalize();
        if (Files.exists(importedPath)) {
          return Optional.of(importedPath);
        }
      }

      Path samePackage = resolveSamePackageClass(sourceContext.file(), className);
      if (samePackage != null) {
        return Optional.of(samePackage);
      }

      return Optional.empty();
    }

    private Map<String, Path> resolveImportedFilesBySuffix(
        SourceContext sourceContext, List<ImportDeclaration> imports, String suffix) {
      Map<String, Path> resolved = new LinkedHashMap<>();
      for (ImportDeclaration importDeclaration : imports) {
        if (importDeclaration.isAsterisk()) {
          continue;
        }
        String importedName = importDeclaration.getName().getIdentifier();
        if (!importedName.endsWith(suffix)) {
          continue;
        }

        Path importedPath =
            sourceContext
                .sourceRoot()
                .resolve(importDeclaration.getNameAsString().replace('.', '/') + ".java")
                .normalize();
        if (Files.exists(importedPath)) {
          resolved.put(importedName, importedPath);
        }
      }
      return resolved;
    }

    private Optional<ClassOrInterfaceDeclaration> findClassDeclaration(
        SourceContext sourceContext, String className) {
      return sourceContext.unit().findAll(ClassOrInterfaceDeclaration.class).stream()
          .filter(declaration -> declaration.getNameAsString().equals(className))
          .findFirst();
    }

    private Optional<String> pathAnnotationValue(NodeWithAnnotations<?> node) {
      return node.getAnnotationByName("Path").flatMap(
          annotation -> {
            if (annotation instanceof SingleMemberAnnotationExpr singleMemberAnnotationExpr) {
              return extractStringLiteral(singleMemberAnnotationExpr.getMemberValue());
            }
            return Optional.empty();
          });
    }

    private boolean hasAnnotation(NodeWithAnnotations<?> node, String annotationName) {
      return node.getAnnotationByName(annotationName).isPresent();
    }

    private Optional<String> extractStringLiteral(Expression expression) {
      if (expression instanceof StringLiteralExpr literalExpr) {
        return Optional.of(literalExpr.getValue());
      }
      return Optional.empty();
    }

    private Optional<String> stringArgument(MethodCallExpr call, int index) {
      if (call.getArguments().size() <= index) {
        return Optional.empty();
      }
      return extractStringLiteral(call.getArgument(index));
    }

    private String scopeName(Expression expression) {
      if (expression instanceof NameExpr nameExpr) {
        return nameExpr.getNameAsString();
      }
      if (expression instanceof FieldAccessExpr fieldAccessExpr) {
        return fieldAccessExpr.getNameAsString();
      }
      return expression.toString();
    }

    private Path resolveSamePackageClass(Path file, String className) {
      Path candidate = file.getParent().resolve(className + ".java").normalize();
      if (Files.exists(candidate)) {
        return candidate;
      }
      return null;
    }

    private Path resolveSourceRoot(Path file, CompilationUnit unit) {
      Optional<String> packageName =
          unit.getPackageDeclaration().map(declaration -> declaration.getNameAsString());
      if (packageName.isEmpty()) {
        return file.getParent();
      }

      int packageSegments = packageName.get().split("\\.").length;
      Path sourceRoot = file.getParent();
      for (int index = 0; index < packageSegments; index++) {
        sourceRoot = sourceRoot.getParent();
      }
      return sourceRoot;
    }

    private void collectTypeNameBySuffix(Set<String> collector, Type type, String suffix) {
      if (!type.isClassOrInterfaceType()) {
        return;
      }

      ClassOrInterfaceType classType = type.asClassOrInterfaceType();
      String typeName = classType.getNameAsString();
      if (typeName.endsWith(suffix)) {
        collector.add(typeName);
      }

      classType.getTypeArguments()
          .ifPresent(arguments -> arguments.forEach(argument -> collectTypeNameBySuffix(collector, argument, suffix)));
    }

    private String key(Path file, String className) {
      return file + "#" + className;
    }

    private static String stripSuffix(String value, String suffix) {
      return value.endsWith(suffix) ? value.substring(0, value.length() - suffix.length()) : value;
    }

    private static String normalizeClassName(String className) {
      return joinTokens(splitTokens(className));
    }

    private static List<String> normalizeRel(String rel) {
      return Arrays.stream(rel.split("[-_]"))
          .filter(part -> !part.isBlank())
          .map(Analyzer::singularize)
          .map(part -> part.toLowerCase(Locale.ROOT))
          .toList();
    }

    private static boolean suffixMatches(List<String> relTokens, List<String> modelTokens) {
      if (relTokens.isEmpty() || modelTokens.isEmpty()) {
        return false;
      }
      if (relTokens.equals(modelTokens)) {
        return true;
      }
      if (relTokens.size() > modelTokens.size()) {
        List<String> suffix =
            relTokens.subList(relTokens.size() - modelTokens.size(), relTokens.size());
        return suffix.equals(modelTokens);
      }
      List<String> suffix =
          modelTokens.subList(modelTokens.size() - relTokens.size(), modelTokens.size());
      return suffix.equals(relTokens);
    }

    private static List<String> splitTokens(String value) {
      String separated = value.replaceAll("([a-z0-9])([A-Z])", "$1 $2");
      return Arrays.stream(separated.split("[\\s_-]+"))
          .filter(part -> !part.isBlank())
          .map(part -> part.toLowerCase(Locale.ROOT))
          .toList();
    }

    private static String joinTokens(List<String> tokens) {
      return String.join("-", tokens);
    }

    private static String singularize(String token) {
      String lower = token.toLowerCase(Locale.ROOT);
      if (lower.endsWith("ies") && lower.length() > 3) {
        return lower.substring(0, lower.length() - 3) + "y";
      }
      if (lower.endsWith("ses") && lower.length() > 3) {
        return lower.substring(0, lower.length() - 2);
      }
      if (lower.endsWith("s") && lower.length() > 1 && !lower.endsWith("ss")) {
        return lower.substring(0, lower.length() - 1);
      }
      return lower;
    }

    private static String joinPaths(String base, String segment) {
      String normalizedBase = normalizePath(base);
      String normalizedSegment = normalizePath(segment);
      if ("/".equals(normalizedSegment)) {
        return normalizedBase;
      }
      if ("/".equals(normalizedBase)) {
        return normalizedSegment;
      }
      return normalizedBase + normalizedSegment;
    }

    private static String normalizePath(String path) {
      if (path == null || path.isBlank() || "/".equals(path)) {
        return "/";
      }

      String normalized = path.startsWith("/") ? path : "/" + path;
      normalized = normalized.replaceAll("/{2,}", "/");
      if (normalized.length() > 1 && normalized.endsWith("/")) {
        normalized = normalized.substring(0, normalized.length() - 1);
      }
      return normalized;
    }
  }

  private record ParsedJavaFile(SourceContext sourceContext, List<ParsedModelClass> modelClasses) {
    private String primaryClassName() {
      String expected = sourceContext.file().getFileName().toString().replace(".java", "");
      return modelClasses.stream()
          .map(ParsedModelClass::name)
          .filter(expected::equals)
          .findFirst()
          .orElseGet(() -> modelClasses.stream().map(ParsedModelClass::name).findFirst().orElseThrow());
    }

    private ParsedModelClass modelClassByName(String name) {
      return modelClasses.stream().filter(parsed -> parsed.name().equals(name)).findFirst().orElse(null);
    }
  }

  private record SourceContext(Path file, Path sourceRoot, CompilationUnit unit) {}

  private record ModelTree(String selfApi, List<ApiModelNode> links) {}

  private record ParsedModelClass(
      String name, List<ParsedLink> links, Map<String, Path> referencedModelFiles) {}

  private record ParsedLink(String rel, String api, String name, Path targetModelFile) {}

  private record BuilderState(String path, Endpoint endpoint) {}

  private record Endpoint(Path modelFile, Path resourceFile) {
    private static Endpoint none() {
      return new Endpoint(null, null);
    }
  }
}
