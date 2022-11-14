package org.eclipse.tractusx.sde.common.submodel.executor.create.steps;

import org.eclipse.tractusx.sde.common.entities.SubmodelFileRequest;
import org.eclipse.tractusx.sde.common.entities.csv.RowData;

import com.google.gson.JsonObject;

public interface ICsvParseStep {

	JsonObject execute(RowData rowData, String processId, SubmodelFileRequest submodelFileRequest);
}