package com.catenax.sde.common.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(Include. NON_NULL)
public class Schema {

	private String type;

	private String title;

	private List<String> required;

	private Map<Object, List<String>> dependentRequired;

	private Map<String, FieldAttributes> properties;

	private List<Map<Object, Object>> examples;

}
