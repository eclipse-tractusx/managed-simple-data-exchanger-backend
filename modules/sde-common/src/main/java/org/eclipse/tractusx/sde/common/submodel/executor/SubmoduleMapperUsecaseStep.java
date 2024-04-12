package org.eclipse.tractusx.sde.common.submodel.executor;

import com.google.gson.JsonObject;

public interface SubmoduleMapperUsecaseStep {

	public void init(JsonObject submodelSchema);
	
	public JsonObject mapJsonbjectToFormatedResponse(JsonObject jsonObject);

}
