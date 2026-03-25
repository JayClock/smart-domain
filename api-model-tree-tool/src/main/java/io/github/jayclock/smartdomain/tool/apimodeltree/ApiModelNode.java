package io.github.jayclock.smartdomain.tool.apimodeltree;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiModelNode(String rel, String api, Boolean cycle, List<ApiModelNode> links) {}
