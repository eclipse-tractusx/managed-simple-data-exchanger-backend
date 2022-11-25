package org.eclipse.tractusx.sde.usecase;

import org.eclipse.tractusx.sde.common.extensions.UsecaseExtension;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

@Component
public class TraceabilityUsecase extends UsecaseExtension {

	private JsonObject useCase;

	public TraceabilityUsecase() {

		useCase = loadUsecae("traceability");

		addSubmodel("Aspect");

		addSubmodel("AspectRelationshiop");

		addSubmodel("Batch");
	}

	@Override
	public JsonObject getUseCase() {
		return this.useCase;
	}

}
