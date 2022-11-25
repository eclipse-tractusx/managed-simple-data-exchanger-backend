package org.eclipse.tractusx.sde.submodels.batch;

import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.eclipse.tractusx.sde.common.extensions.SubmodelExtension;
import org.eclipse.tractusx.sde.common.model.Submodel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BatchSubmodel extends SubmodelExtension {

	private Submodel submodel = null;

	@Autowired
	private BatchExecutor batchWorkflow;

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

		submodel.setExecutor(batchWorkflow);
	}

	@Override
	public Submodel submodel() {
		return submodel;
	}

}