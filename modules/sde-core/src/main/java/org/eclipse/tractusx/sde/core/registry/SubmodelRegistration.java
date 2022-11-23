package org.eclipse.tractusx.sde.core.registry;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.tractusx.sde.common.extensions.SubmodelExtension;
import org.eclipse.tractusx.sde.common.model.Submodel;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SubmodelRegistration {

	private final List<Submodel> submodelList;


	public SubmodelRegistration() {
		submodelList = new LinkedList<>();
	}

	public void register(SubmodelExtension subomdelService) {
		Submodel submodel = subomdelService.submodel();
		log.info(submodel.toString());
		submodelList.add(submodel);
	}

	public List<Submodel> getModels() {
		return this.submodelList;
	}

}
