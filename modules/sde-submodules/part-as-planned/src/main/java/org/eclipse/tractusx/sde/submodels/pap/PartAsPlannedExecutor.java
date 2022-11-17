package org.eclipse.tractusx.sde.submodels.pap;

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.csv.RowData;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerDigitalTwinUseCaseException;
import org.eclipse.tractusx.sde.common.submodel.executor.SubmodelExecutor;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.CsvParse;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.GenerateUrnUUID;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.JsonRecordValidate;
import org.eclipse.tractusx.sde.submodels.pap.mapper.PartAsPlannedMapper;
import org.eclipse.tractusx.sde.submodels.pap.model.PartAsPlanned;
import org.eclipse.tractusx.sde.submodels.pap.services.PartAsPlannedService;
import org.eclipse.tractusx.sde.submodels.pap.steps.DigitalTwinsPartAsPlannedCsvHandlerUseCase;
import org.eclipse.tractusx.sde.submodels.pap.steps.EDCPartAsPlannedHandlerUseCase;
import org.eclipse.tractusx.sde.submodels.pap.steps.StorePartAsPlannedCsvHandlerUseCase;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class PartAsPlannedExecutor extends SubmodelExecutor {
	
	private final PartAsPlannedMapper partAsPlannedMapper;

	private final CsvParse csvParseStep;

	private final GenerateUrnUUID generateUrnUUID;

	private final JsonRecordValidate jsonRecordValidate;

	private final DigitalTwinsPartAsPlannedCsvHandlerUseCase digitalTwinsPartAsPlannedCsvHandlerUseCase;

	private final EDCPartAsPlannedHandlerUseCase eDCPartAsPlannedHandlerUseCase;

	private final StorePartAsPlannedCsvHandlerUseCase storePartAsPlannedCsvHandlerUseCase;

	private final PartAsPlannedService partAsPlannedService;


	@SneakyThrows
	public void executeCsvRecord(RowData rowData, JsonObject jsonObject, String processId) {
		
		csvParseStep.init(getSubmodelSchema());
		csvParseStep.run(rowData, jsonObject, processId);

		nextSteps(jsonObject, processId);

	}

	
	@SneakyThrows
	public void executeJsonRecord(Integer rowIndex, JsonObject jsonObject, String processId) {

		jsonRecordValidate.init(getSubmodelSchema());
		jsonRecordValidate.run(rowIndex, jsonObject, processId);

		nextSteps(jsonObject, processId);

	}

	
	private void nextSteps(JsonObject jsonObject, String processId) throws CsvHandlerDigitalTwinUseCaseException {


		generateUrnUUID.run(jsonObject, processId);

		PartAsPlanned partAsPlannedAspect = partAsPlannedMapper.mapFrom(jsonObject);

		digitalTwinsPartAsPlannedCsvHandlerUseCase.init(getSubmodelSchema());
		digitalTwinsPartAsPlannedCsvHandlerUseCase.run(partAsPlannedAspect);

		eDCPartAsPlannedHandlerUseCase.init(getSubmodelSchema());
		eDCPartAsPlannedHandlerUseCase.run(getNameOfModel(), partAsPlannedAspect, processId);

		storePartAsPlannedCsvHandlerUseCase.run(partAsPlannedAspect);
	}

	@Override
	public void executeDeleteRecord(JsonObject jsonObject, String delProcessId, String refProcessId) {
		partAsPlannedService.deleteAllDataBySequence(jsonObject);
	}

	@Override
	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {
		return partAsPlannedService.readCreatedTwinsforDelete(refProcessId);
	}

	@Override
	public JsonObject readCreatedTwinsDetails(String uuid) {
		return partAsPlannedService.readCreatedTwinsDetails(uuid);
	}

}
