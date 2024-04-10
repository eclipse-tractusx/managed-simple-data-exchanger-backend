package org.eclipse.tractusx.sde.core.submodel.executor;

import java.util.List;

import org.eclipse.tractusx.sde.bpndiscovery.handler.BPNDiscoveryUseCaseHandler;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.entities.csv.RowData;
import org.eclipse.tractusx.sde.common.submodel.executor.SubmodelExecutor;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.CsvParse;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.GenerateUrnUUID;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.JsonRecordFormating;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.JsonRecordValidate;
import org.eclipse.tractusx.sde.core.submodel.executor.step.DatabaseUsecaseHandler;
import org.eclipse.tractusx.sde.core.submodel.executor.step.DigitalTwinUseCaseHandler;
import org.eclipse.tractusx.sde.core.submodel.executor.step.EDCUsecaseHandler;
import org.eclipse.tractusx.sde.core.submodel.executor.step.SubmoduleResponseHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Component
@RequiredArgsConstructor
public class GenericSubmodelExecutor extends SubmodelExecutor {

	private final CsvParse csvParseStep;
	private final JsonRecordFormating jsonRecordformater;
	private final GenerateUrnUUID generateUrnUUID;
	private final JsonRecordValidate jsonRecordValidate;
	private final DigitalTwinUseCaseHandler digitalTwinUseCaseHandler;
	private final EDCUsecaseHandler eDCAspectHandlerUseCase;
	private final BPNDiscoveryUseCaseHandler bPNDiscoveryUseCaseHandler;
	private final DatabaseUsecaseHandler databaseUsecaseHandler;
	private final SubmoduleResponseHandler submoduleResponseHandler;

	@SneakyThrows
	@Override
	public void executeCsvRecord(RowData rowData, ObjectNode jsonObject, String processId, PolicyModel policy) {

		csvParseStep.init(getSubmodelSchema());
		csvParseStep.run(rowData, jsonObject, processId);

		nextSteps(rowData.position(), jsonObject, processId, policy);

	}

	@SneakyThrows
	@Override
	public void executeJsonRecord(Integer rowIndex, ObjectNode jsonObject, String processId, PolicyModel policy) {

		jsonRecordformater.init(getSubmodelSchema());
		jsonRecordformater.run(rowIndex, jsonObject, processId);

		nextSteps(rowIndex, jsonObject, processId, policy);

	}

	@SneakyThrows
	private void nextSteps(Integer rowIndex, ObjectNode jsonObject, String processId, PolicyModel policy) {

		generateUrnUUID.run(jsonObject, processId);

		jsonRecordValidate.init(getSubmodelSchema());
		jsonRecordValidate.run(rowIndex, jsonObject);

		digitalTwinUseCaseHandler.init(getSubmodelSchema());
		digitalTwinUseCaseHandler.run(jsonObject, policy);

		eDCAspectHandlerUseCase.init(getSubmodelSchema());
		eDCAspectHandlerUseCase.run(rowIndex, jsonObject, processId, policy);

		bPNDiscoveryUseCaseHandler.init(getSubmodelSchema());
		bPNDiscoveryUseCaseHandler.run(jsonObject);

		databaseUsecaseHandler.init(getSubmodelSchema());
		databaseUsecaseHandler.run(jsonObject, processId);
	}

	@Override
	public void executeDeleteRecord(JsonObject jsonObject, String delProcessId, String refProcessId) {
		eDCAspectHandlerUseCase.init(getSubmodelSchema());
		eDCAspectHandlerUseCase.deleteEDCAsset(jsonObject);
		digitalTwinUseCaseHandler.init(getSubmodelSchema());
		digitalTwinUseCaseHandler.deleteSubmodelfromShellById(jsonObject);
		databaseUsecaseHandler.init(getSubmodelSchema());
		databaseUsecaseHandler.saveSubmoduleWithDeleted(jsonObject, delProcessId, refProcessId);
	}

	@Override
	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {
		databaseUsecaseHandler.init(getSubmodelSchema());
		return databaseUsecaseHandler.readCreatedTwinsforDelete(refProcessId);
	}

	@Override
	public JsonObject readCreatedTwinsDetails(String uuid) {
		databaseUsecaseHandler.init(getSubmodelSchema());
		return submoduleResponseHandler.mapJsonbjectToFormatedResponse(databaseUsecaseHandler.readCreatedTwinsDetails(uuid));
	}

	@Override
	public int getUpdatedRecordCount(String processId) {
		databaseUsecaseHandler.init(getSubmodelSchema());
		return databaseUsecaseHandler.getUpdatedData(processId);
	}
	
	


}
