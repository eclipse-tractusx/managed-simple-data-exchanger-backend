package org.eclipse.tractusx.sde.common.submodel.executor;

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.csv.RowData;

import com.google.gson.JsonObject;

import lombok.Getter;

public abstract class SubmodelExecutor {

	@Getter
	JsonObject submodelSchema;

	public void init(JsonObject submodelSchema) {
		this.submodelSchema = submodelSchema;
	}
	
	public String getNameOfModel() {
		return this.submodelSchema.get("id").getAsString();
	}

	public abstract void executeCsvRecord(RowData rowData, JsonObject jsonObject, String processId);

	public abstract void executeJsonRecord(Integer rowIndex, JsonObject jsonObject, String processId);

	public abstract List<JsonObject> readCreatedTwinsforDelete(String refProcessId);

	public abstract void executeDeleteRecord(JsonObject jsonObject, String delProcessId, String refProcessId);

	public abstract JsonObject readCreatedTwinsDetails(String uuid);

}
