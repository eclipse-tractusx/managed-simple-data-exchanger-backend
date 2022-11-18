package org.eclipse.tractusx.sde.submodels.slbap;

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.csv.RowData;
import org.eclipse.tractusx.sde.common.submodel.executor.SubmodelExecutor;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.CsvParse;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.GenerateUrnUUID;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.JsonRecordValidate;
import org.eclipse.tractusx.sde.submodels.slbap.mapper.SingleLevelBoMAsPlannedMapper;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SingleLevelBoMAsPlannedExecutor extends SubmodelExecutor {
	
	private final SingleLevelBoMAsPlannedMapper singleLevelBoMAsPlannedMapper;

	private final CsvParse csvParseStep;

	private final GenerateUrnUUID generateUrnUUID;

	private final JsonRecordValidate jsonRecordValidate;

	//private final DigitalTwinsSingleLevelBoMAsPlannedHandlerStep digitalTwinsHandlerStep;

	//private final EDCSingleLevelBoMAsPlannedHandlerStep eDCHandlerStep;

	//private final StoreSingleLevelBoMAsPlannedStep storeSingleLevelBoMAsPlannedStep;

	//private final SingleLevelBoMAsPlannedService singleLevelBoMAsPlannedService;

	
	@Override
	public void executeCsvRecord(RowData rowData, ObjectNode jsonObject, String processId) {
		
		csvParseStep.init(getSubmodelSchema());

		csvParseStep.run(rowData, jsonObject, processId);

		//nextSteps(jsonObject, processId);
	}

	@Override
	public void executeJsonRecord(Integer rowIndex, ObjectNode jsonObject, String processId) {
		
		jsonRecordValidate.init(getSubmodelSchema());
		jsonRecordValidate.run(rowIndex, jsonObject);

		//nextSteps(jsonObject, processId);
	}
	
//	private void nextSteps(ObjectNode jsonObject, String processId) throws CsvHandlerDigitalTwinUseCaseException {
//
//		SingleLevelBoMAsPlanned singleLevelBoMAsPlanned = singleLevelBoMAsPlannedMapper.mapFrom(jsonObject);
//
//		generateUrnUUID.run(aspectRelationship, processId);
//
//		digitalTwinsAspectRelationShipCsvHandlerUseCase.run(aspectRelationship);
//
//		eDCAspectRelationshipHandlerUseCase.run(getNameOfModel(), aspectRelationship, processId);
//
//		storeAspectRelationshipCsvHandlerUseCase.run(aspectRelationship);
//	}

	@Override
	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {
		return null;
	}

	@Override
	public void executeDeleteRecord(JsonObject jsonObject, String delProcessId, String refProcessId) {
		
	}

	@Override
	public JsonObject readCreatedTwinsDetails(String uuid) {
		return null;
	}
}
