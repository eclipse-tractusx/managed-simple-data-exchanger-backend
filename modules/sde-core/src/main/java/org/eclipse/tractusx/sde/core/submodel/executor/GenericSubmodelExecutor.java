package org.eclipse.tractusx.sde.core.submodel.executor;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.entities.csv.RowData;
import org.eclipse.tractusx.sde.common.submodel.executor.BPNDiscoveryUsecaseStep;
import org.eclipse.tractusx.sde.common.submodel.executor.DatabaseUsecaseStep;
import org.eclipse.tractusx.sde.common.submodel.executor.DigitalTwinUsecaseStep;
import org.eclipse.tractusx.sde.common.submodel.executor.EDCUsecaseStep;
import org.eclipse.tractusx.sde.common.submodel.executor.SubmodelExecutor;
import org.eclipse.tractusx.sde.common.submodel.executor.SubmoduleMapperUsecaseStep;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.CsvParse;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.GenerateUrnUUID;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.JsonRecordFormating;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.JsonRecordValidate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Component
public class GenericSubmodelExecutor extends SubmodelExecutor {

	private final CsvParse csvParseStep;
	private final JsonRecordFormating jsonRecordformater;
	private final GenerateUrnUUID generateUrnUUID;
	private final JsonRecordValidate jsonRecordValidate;

	@Qualifier("DigitalTwinUseCaseHandler")
	private final DigitalTwinUsecaseStep digitalTwinUseCaseStep;

	@Qualifier("EDCUsecaseHandler")
	private final EDCUsecaseStep edcUseCaseStep;

	@Qualifier("BPNDiscoveryUseCaseHandler")
	private final BPNDiscoveryUsecaseStep bpnUseCaseTwinStep;

	@Qualifier("DatabaseUsecaseHandler")
	private final DatabaseUsecaseStep databaseUseCaseStep;

	@Qualifier("SubmoduleResponseHandler")
	private final SubmoduleMapperUsecaseStep submodelMapperUseCaseStep;

	public GenericSubmodelExecutor(CsvParse csvParseStep, JsonRecordFormating jsonRecordformater,
			GenerateUrnUUID generateUrnUUID, JsonRecordValidate jsonRecordValidate,
			@Qualifier("DigitalTwinUseCaseHandler") DigitalTwinUsecaseStep digitalTwinUseCaseStep,
			@Qualifier("EDCUsecaseHandler") EDCUsecaseStep edcUseCaseStep,
			@Qualifier("BPNDiscoveryUseCaseHandler") BPNDiscoveryUsecaseStep bpnUseCaseTwinStep,
			@Qualifier("DatabaseUsecaseHandler") DatabaseUsecaseStep databaseUseCaseStep,
			@Qualifier("SubmoduleResponseHandler") SubmoduleMapperUsecaseStep submodelMapperUseCaseStep) {
		this.csvParseStep = csvParseStep;
		this.jsonRecordformater = jsonRecordformater;
		this.generateUrnUUID = generateUrnUUID;
		this.jsonRecordValidate = jsonRecordValidate;
		this.digitalTwinUseCaseStep = digitalTwinUseCaseStep;
		this.edcUseCaseStep = edcUseCaseStep;
		this.bpnUseCaseTwinStep = bpnUseCaseTwinStep;
		this.databaseUseCaseStep = databaseUseCaseStep;
		this.submodelMapperUseCaseStep = submodelMapperUseCaseStep;
	}

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

		getDtExecutorStep().init(getSubmodelSchema());
		getDtExecutorStep().run(rowIndex, jsonObject, processId, policy);

		getEDCExecutorStep().init(getSubmodelSchema());
		getEDCExecutorStep().run(rowIndex, jsonObject, processId, policy);

		getBpnExecutorStep().init(getSubmodelSchema());
		getBpnExecutorStep().run(rowIndex, jsonObject, processId, policy);

		getDatabaseExecutorStep().init(getSubmodelSchema());
		getDatabaseExecutorStep().run(rowIndex, jsonObject, processId, policy);
	}

	@Override
	public void executeDeleteRecord(Integer rowIndex, JsonObject jsonObject, String delProcessId, String refProcessId) {
		getEDCExecutorStep().init(getSubmodelSchema());
		getEDCExecutorStep().delete(rowIndex, jsonObject, delProcessId, refProcessId);
		getDtExecutorStep().init(getSubmodelSchema());
		getDtExecutorStep().delete(rowIndex, jsonObject, delProcessId, refProcessId);
		getDatabaseExecutorStep().init(getSubmodelSchema());
		getDatabaseExecutorStep().saveSubmoduleWithDeleted(rowIndex, jsonObject, delProcessId, refProcessId);
	}

	@Override
	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {
		getDatabaseExecutorStep().init(getSubmodelSchema());
		return getDatabaseExecutorStep().readCreatedTwins(refProcessId, CommonConstants.DELETED_Y);
	}

	@Override
	public List<JsonObject> readCreatedTwinsByProcessId(String refProcessId) {
		getDatabaseExecutorStep().init(getSubmodelSchema());
		return getDatabaseExecutorStep().readCreatedTwins(refProcessId, null);
	}

	@Override
	public JsonObject readCreatedTwinsDetails(String uuid) {
		getDatabaseExecutorStep().init(getSubmodelSchema());
		getSubmodelMapperExecutorStep().init(getSubmodelSchema());
		return getSubmodelMapperExecutorStep()
				.mapJsonbjectToFormatedResponse(getDatabaseExecutorStep().readCreatedTwinsDetails(uuid));
	}

	@Override
	public int getUpdatedRecordCount(String processId) {
		getDatabaseExecutorStep().init(getSubmodelSchema());
		return getDatabaseExecutorStep().getUpdatedData(processId);
	}

	private DigitalTwinUsecaseStep getDtExecutorStep() {
		return Optional.ofNullable(submodel.getDigitalTwinUseCaseStep()).orElse(digitalTwinUseCaseStep);
	}

	private EDCUsecaseStep getEDCExecutorStep() {
		return Optional.ofNullable(submodel.getEdcUseCaseStep()).orElse(edcUseCaseStep);
	}

	private BPNDiscoveryUsecaseStep getBpnExecutorStep() {
		return Optional.ofNullable(submodel.getBpnUseCaseTwinStep()).orElse(bpnUseCaseTwinStep);
	}

	private DatabaseUsecaseStep getDatabaseExecutorStep() {
		return Optional.ofNullable(submodel.getDatabaseUseCaseStep()).orElse(databaseUseCaseStep);
	}

	private SubmoduleMapperUsecaseStep getSubmodelMapperExecutorStep() {
		return Optional.ofNullable(submodel.getSubmodelMapperUseCaseStep()).orElse(submodelMapperUseCaseStep);
	}

}
