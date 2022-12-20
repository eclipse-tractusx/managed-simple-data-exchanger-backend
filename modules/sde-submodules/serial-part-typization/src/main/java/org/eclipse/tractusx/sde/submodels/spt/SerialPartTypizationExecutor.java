package org.eclipse.tractusx.sde.submodels.spt;

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.csv.RowData;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerDigitalTwinUseCaseException;
import org.eclipse.tractusx.sde.common.submodel.executor.SubmodelExecutor;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.CsvParse;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.GenerateUrnUUID;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.JsonRecordValidate;
import org.eclipse.tractusx.sde.submodels.spt.mapper.AspectMapper;
import org.eclipse.tractusx.sde.submodels.spt.model.Aspect;
import org.eclipse.tractusx.sde.submodels.spt.service.AspectService;
import org.eclipse.tractusx.sde.submodels.spt.steps.DigitalTwinsAspectCsvHandlerUseCase;
import org.eclipse.tractusx.sde.submodels.spt.steps.EDCAspectHandlerUseCase;
import org.eclipse.tractusx.sde.submodels.spt.steps.StoreAspectCsvHandlerUseCase;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class SerialPartTypizationExecutor extends SubmodelExecutor {

	private final AspectMapper aspectMapper;

	private final CsvParse csvParseStep;

	private final GenerateUrnUUID generateUrnUUID;

	private final JsonRecordValidate jsonRecordValidate;

	private final DigitalTwinsAspectCsvHandlerUseCase digitalTwinsAspectCsvHandlerUseCase;

	private final EDCAspectHandlerUseCase eDCAspectHandlerUseCase;

	private final StoreAspectCsvHandlerUseCase storeAspectCsvHandlerUseCase;

	private final AspectService aspectService;

	@SneakyThrows
	public void executeCsvRecord(RowData rowData, ObjectNode jsonObject, String processId) {

		csvParseStep.init(getSubmodelSchema());
		csvParseStep.run(rowData, jsonObject, processId);

		nextSteps(rowData.position(), jsonObject, processId);

	}

	@SneakyThrows
	public void executeJsonRecord(Integer rowIndex, ObjectNode jsonObject, String processId) {

		nextSteps(rowIndex, jsonObject, processId);

	}

	private void nextSteps(Integer rowIndex, ObjectNode jsonObject, String processId)
			throws CsvHandlerDigitalTwinUseCaseException {

		generateUrnUUID.run(jsonObject, processId);

		jsonRecordValidate.init(getSubmodelSchema());
		jsonRecordValidate.run(rowIndex, jsonObject);

		Aspect aspect = aspectMapper.mapFrom(jsonObject);

		digitalTwinsAspectCsvHandlerUseCase.init(getSubmodelSchema());
		digitalTwinsAspectCsvHandlerUseCase.run(aspect);

		eDCAspectHandlerUseCase.init(getSubmodelSchema());
		eDCAspectHandlerUseCase.run(getNameOfModel(), aspect, processId);
		

		storeAspectCsvHandlerUseCase.run(aspect);
	}

	@Override
	public void executeDeleteRecord(JsonObject jsonObject, String delProcessId, String refProcessId) {
		aspectService.deleteAllDataBySequence(jsonObject);
	}

	@Override
	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {
		return aspectService.readCreatedTwinsforDelete(refProcessId);
	}

	@Override
	public JsonObject readCreatedTwinsDetails(String uuid) {
		return aspectService.readCreatedTwinsDetails(uuid);
	}

	@Override
	public int getUpdatedRecordCount(String processId) {
		return aspectService.getUpdatedData(processId);
	}

}
