package org.eclipse.tractusx.sde.submodels.apr;

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.csv.RowData;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerDigitalTwinUseCaseException;
import org.eclipse.tractusx.sde.common.submodel.executor.SubmodelExecutor;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.CsvParse;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.JsonRecordValidate;
import org.eclipse.tractusx.sde.submodels.apr.mapper.AspectRelationshipMapper;
import org.eclipse.tractusx.sde.submodels.apr.model.AspectRelationship;
import org.eclipse.tractusx.sde.submodels.apr.service.AspectRelationshipService;
import org.eclipse.tractusx.sde.submodels.apr.steps.AssemblyPartRelationshipUUIDUrnUUID;
import org.eclipse.tractusx.sde.submodels.apr.steps.DigitalTwinsAspectRelationShipCsvHandlerUseCase;
import org.eclipse.tractusx.sde.submodels.apr.steps.EDCAspectRelationshipHandlerUseCase;
import org.eclipse.tractusx.sde.submodels.apr.steps.StoreAspectRelationshipCsvHandlerUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Component
@AllArgsConstructor
public class AssemblyPartRelationshipExecutor extends SubmodelExecutor {

	@Autowired
	private final CsvParse csvParseStep;

	private final JsonRecordValidate jsonRecordValidate;

	private final AssemblyPartRelationshipUUIDUrnUUID generateUrnUUID;

	private final DigitalTwinsAspectRelationShipCsvHandlerUseCase digitalTwinsAspectRelationShipCsvHandlerUseCase;

	private final EDCAspectRelationshipHandlerUseCase eDCAspectRelationshipHandlerUseCase;

	private final StoreAspectRelationshipCsvHandlerUseCase storeAspectRelationshipCsvHandlerUseCase;

	private final AspectRelationshipMapper aspectRelationshipMapper;

	private final AspectRelationshipService aspectRelationshipService;

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

		AspectRelationship aspectRelationship = aspectRelationshipMapper.mapFrom(jsonObject);

		generateUrnUUID.run(aspectRelationship, processId);

		digitalTwinsAspectRelationShipCsvHandlerUseCase.run(aspectRelationship);

		eDCAspectRelationshipHandlerUseCase.run(getNameOfModel(), aspectRelationship, processId);

		storeAspectRelationshipCsvHandlerUseCase.run(aspectRelationship);
	}

	@Override
	public void executeDeleteRecord(JsonObject jsonObject, String delProcessId, String refProcessId) {
		aspectRelationshipService.deleteAllDataBySequence(jsonObject);
	}

	@Override
	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {
		return aspectRelationshipService.readCreatedTwinsforDelete(refProcessId);
	}

	@Override
	public JsonObject readCreatedTwinsDetails(String uuid) {
		return aspectRelationshipService.readCreatedTwinsDetails(uuid);
	}
}
