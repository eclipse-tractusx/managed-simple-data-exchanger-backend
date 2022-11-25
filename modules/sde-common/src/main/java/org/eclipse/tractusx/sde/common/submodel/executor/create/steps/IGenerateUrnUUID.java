package org.eclipse.tractusx.sde.common.submodel.executor.create.steps;

import com.google.gson.JsonObject;

public interface IGenerateUrnUUID {

	JsonObject execute(JsonObject jsonObject, String processId);
}
