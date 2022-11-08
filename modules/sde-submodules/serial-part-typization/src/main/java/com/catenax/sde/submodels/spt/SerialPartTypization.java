package com.catenax.sde.submodels.spt;

import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.catenax.sde.common.extensions.SubmodelExtension;
import com.google.gson.JsonObject;

@Component
public class SerialPartTypization extends SubmodelExtension {

	private JsonObject submodel = null;

	@PostConstruct
	public void init() {

		String resource = "serial-part-typization.json";
		// this is the path within the jar file
		InputStream input = this.getClass().getResourceAsStream("/resources/" + resource);
		if (input == null) {
			// this is how we load file within editor (eg eclipse)
			input = this.getClass().getClassLoader().getResourceAsStream(resource);
		}

		submodel = loadSubmodel(input);
		
		//AdddigitalTwinMappnig
		
		//EDCmmaping

	}

	public JsonObject submodel() {
		return this.submodel;
	}

}
