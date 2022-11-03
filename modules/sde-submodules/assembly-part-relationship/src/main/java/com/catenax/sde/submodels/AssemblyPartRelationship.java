package com.catenax.sde.submodels;

import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.catenax.sde.common.extensions.SubmodelExtension;
import com.google.gson.JsonObject;

@Component
public class AssemblyPartRelationship extends SubmodelExtension {

	private JsonObject submodel = null;

	@PostConstruct
	public void init() {

		String resource = "assembly-part-relationship.json";
		// this is the path within the jar file
		InputStream input = this.getClass().getResourceAsStream("/resources/" + resource);
		if (input == null) {
			// this is how we load file within editor (eg eclipse)
			input = this.getClass().getClassLoader().getResourceAsStream(resource);
		}

		submodel = loadSubmodel(input);
		
		//Mapp POJO for Digital twin (UUID+manufacturingI="shellId")
		
		//load custom logic 
	}

	@Override
	public JsonObject submodel() {
		return this.submodel;
	}

}
