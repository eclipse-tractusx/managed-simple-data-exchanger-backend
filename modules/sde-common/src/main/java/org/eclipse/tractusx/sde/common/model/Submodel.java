package org.eclipse.tractusx.sde.common.model;

import org.eclipse.tractusx.sde.common.submodel.executor.SubmodelExecutor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.gson.JsonObject;

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
public class Submodel {

	private String id;
	
	private String name;

	private String version;
	
	private String usecases;
	
	private JsonObject schema;

	private SubmodelExecutor executor;

}
