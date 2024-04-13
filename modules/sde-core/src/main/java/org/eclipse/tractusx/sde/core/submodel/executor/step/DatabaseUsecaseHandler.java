package org.eclipse.tractusx.sde.core.submodel.executor.step;

import java.util.List;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.common.model.Submodel;
import org.eclipse.tractusx.sde.common.submodel.executor.DatabaseUsecaseStep;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.core.processreport.repository.SubmodelCustomHistoryGenerator;
import org.eclipse.tractusx.sde.core.service.SubmodelService;
import org.eclipse.tractusx.sde.core.utils.SubmoduleUtility;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service("DatabaseUsecaseHandler")
@RequiredArgsConstructor
public class DatabaseUsecaseHandler extends Step implements DatabaseUsecaseStep {

	private final SubmodelCustomHistoryGenerator submodelCustomHistoryGenerator;

	private final SubmodelService submodelService;
	private final SubmoduleUtility submoduleUtility;

	@SneakyThrows
	public JsonNode run(Integer rowIndex, ObjectNode jsonObject, String processId, PolicyModel policy) {

		Submodel schemaObj = submodelService.findSubmodelByNameAsSubmdelObject(getNameOfModel());
		List<String> columns = submoduleUtility.getTableColomnHeader(schemaObj);
		String tableName = submoduleUtility.getTableName(schemaObj);

		submodelCustomHistoryGenerator.saveSubmodelData(columns, tableName, processId, jsonObject,
				getIdentifierOfModel());

		return jsonObject;
	}

	@Override
	public void delete(Integer rowIndex, JsonObject jsonObject, String delProcessId, String refProcessId) {
	}

	@SneakyThrows
	public void saveSubmoduleWithDeleted(JsonObject jsonObject, String delProcessId, String refProcessId) {

		String uuid = jsonObject.get(getIdentifierOfModel()).getAsString();
		Submodel schemaObj = submodelService.findSubmodelByNameAsSubmdelObject(getNameOfModel());
		String tableName = submoduleUtility.getTableName(schemaObj);

		submodelCustomHistoryGenerator.saveAspectWithDeleted(uuid, tableName, getIdentifierOfModel());
	}

	@SneakyThrows
	public List<JsonObject> readCreatedTwins(String refProcessId, String isDeleted) {
		Submodel schemaObj = submodelService.findSubmodelByNameAsSubmdelObject(getNameOfModel());
		List<String> columns = submoduleUtility.getTableColomnHeader(schemaObj);
		String tableName = submoduleUtility.getTableName(schemaObj);

		List<JsonObject> allSubmoduleAsJsonList = submodelCustomHistoryGenerator.findAllSubmoduleAsJsonList(columns,
				tableName, refProcessId, isDeleted);

		if (allSubmoduleAsJsonList.isEmpty())
			throw new NoDataFoundException("No data founds for deletion, All records are already deleted");
		return allSubmoduleAsJsonList;
	}

	@SneakyThrows
	public JsonObject readCreatedTwinsDetails(String uuid) {
		Submodel schemaObj = submodelService.findSubmodelByNameAsSubmdelObject(getNameOfModel());
		List<String> columns = submoduleUtility.getTableColomnHeader(schemaObj);
		String tableName = submoduleUtility.getTableName(schemaObj);
		return submodelCustomHistoryGenerator.readCreatedTwinsDetails(columns, tableName, uuid, getIdentifierOfModel());
	}

	@SneakyThrows
	public int getUpdatedData(String processId) {
		Submodel schemaObj = submodelService.findSubmodelByNameAsSubmdelObject(getNameOfModel());
		String tableName = submoduleUtility.getTableName(schemaObj);
		return submodelCustomHistoryGenerator.countUpdatedRecordCount(tableName, CommonConstants.UPDATED_Y, processId);
	}

}
