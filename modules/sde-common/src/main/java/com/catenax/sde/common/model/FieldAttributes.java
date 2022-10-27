package com.catenax.sde.common.model;

import java.util.List;

import com.catenax.sde.common.model.validation.ValidationRule;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(Include. NON_NULL)
public class FieldAttributes {
	
	private String type;
	
	private String title;
	
	private String value;
	
	private String format;
	
	private String pattern;
	
	private FieldValueType fieldValueType;
	
	private List<String> examples;
	
	private List<ValidationRule> validations;

}
