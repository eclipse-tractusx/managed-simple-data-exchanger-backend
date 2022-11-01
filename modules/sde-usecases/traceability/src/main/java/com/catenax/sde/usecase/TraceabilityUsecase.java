package com.catenax.sde.usecase;

import org.json.JSONObject;

import com.catenax.sde.common.extensions.UsecaseExtension;

public interface TraceabilityUsecase extends UsecaseExtension {

	@Override
	default JSONObject useCase() {
		
		loadUsecae("traceability");
		
		discoverAndAddsubmodel("Aspect");
		
		discoverAndAddsubmodel("AspectRelationshiop");
		
		discoverAndAddsubmodel("Batch");
		
		return this.useCase();
	}

}
