package com.catenax.sde.usecase;

import com.catenax.sde.common.extensions.UsecaseExtension;
import com.google.gson.JsonObject;

public interface TraceabilityUsecase extends UsecaseExtension {

	@Override
	default JsonObject useCase() {
		
		loadUsecae("traceability");
		
		discoverAndAddsubmodel("Aspect");
		
		discoverAndAddsubmodel("AspectRelationshiop");
		
		discoverAndAddsubmodel("Batch");
		
		return this.useCase();
	}

}
