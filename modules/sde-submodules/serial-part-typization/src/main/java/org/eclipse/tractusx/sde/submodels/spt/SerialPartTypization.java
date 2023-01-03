package org.eclipse.tractusx.sde.submodels.spt;

import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.eclipse.tractusx.sde.common.extensions.SubmodelExtension;
import org.eclipse.tractusx.sde.common.model.Submodel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SerialPartTypization extends SubmodelExtension {

	private Submodel submodel = null;

	@Autowired
	private SerialPartTypizationExecutor serialPartTypizationWorkflow;

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
		submodel.setExecutor(serialPartTypizationWorkflow);
	}

	@Override
	public Submodel submodel() {
		return submodel;
	}

}
