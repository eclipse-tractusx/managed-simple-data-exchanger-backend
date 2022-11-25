package org.eclipse.tractusx.sde.common.submodel.executor;

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.csv.RowData;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
	
	public JsonObject getSubmodelItems() {
		return submodelSchema.get("items").getAsJsonObject();
	}

	public abstract void executeCsvRecord(RowData rowData, ObjectNode jsonObject, String processId);

	public abstract void executeJsonRecord(Integer rowIndex, ObjectNode jsonObject, String processId);

	public abstract List<JsonObject> readCreatedTwinsforDelete(String refProcessId);

	public abstract void executeDeleteRecord(JsonObject jsonObject, String delProcessId, String refProcessId);

	public abstract JsonObject readCreatedTwinsDetails(String uuid);

	public abstract int  getUpdatedRecordCount(String processId);

}
