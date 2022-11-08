package com.catenax.sde.submodels.batch;

import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.catenax.sde.common.extensions.SubmodelExtension;
import com.google.gson.JsonObject;

@Component
public class Batch extends SubmodelExtension {

	private JsonObject submodel = null;

	@PostConstruct
	public void init() {

		String resource = "batch.json";
		// this is the path within the jar file
		InputStream input = this.getClass().getResourceAsStream("/resources/" + resource);
		if (input == null) {
			// this is how we load file within editor (eg eclipse)
			input = this.getClass().getClassLoader().getResourceAsStream(resource);
		}

		submodel = loadSubmodel(input);
	}

	@Override
	public JsonObject submodel() {
		return this.submodel;
	}

}