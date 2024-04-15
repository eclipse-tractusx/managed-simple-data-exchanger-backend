package org.eclipse.tractusx.sde.submodels.slbab;

import java.io.InputStream;

import org.eclipse.tractusx.sde.common.extensions.SubmodelExtension;
import org.eclipse.tractusx.sde.common.model.Submodel;

import jakarta.annotation.PostConstruct;

public class SingleLevelBomAsBuiltV300 extends SubmodelExtension {
	private Submodel submodel = null;

	@PostConstruct
	public void init() {

		String resource = "single-level-bom-as-built-v3.0.0.json";
		// this is the path within the jar file
		InputStream input = this.getClass().getResourceAsStream("/resources/" + resource);
		if (input == null) {
			// this is how we load file within editor (eg eclipse)
			input = this.getClass().getClassLoader().getResourceAsStream(resource);
		}

		submodel = loadSubmodel(input);
		
		submodel.addProperties("tableName", "singlelevelbomasbuilt_v_300");
	}

	@Override
	public Submodel submodel() {
		return submodel;
	}
}
