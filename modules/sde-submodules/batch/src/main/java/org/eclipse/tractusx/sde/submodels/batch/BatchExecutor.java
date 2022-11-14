package org.eclipse.tractusx.sde.submodels.batch;

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.csv.RowData;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerDigitalTwinUseCaseException;
import org.eclipse.tractusx.sde.common.submodel.executor.SubmodelExecutor;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.CsvParse;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.GenerateUrnUUID;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.JsonRecordValidate;
import org.eclipse.tractusx.sde.submodels.batch.mapper.BatchMapper;
import org.eclipse.tractusx.sde.submodels.batch.model.Batch;
import org.eclipse.tractusx.sde.submodels.batch.service.BatchDeleteService;
import org.eclipse.tractusx.sde.submodels.batch.steps.DigitalTwinsBatchCsvHandlerUseCase;
import org.eclipse.tractusx.sde.submodels.batch.steps.EDCBatchHandlerUseCase;
import org.eclipse.tractusx.sde.submodels.batch.steps.StoreBatchCsvHandlerUseCase;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Component
@AllArgsConstructor
public class BatchExecutor extends SubmodelExecutor {

	private final CsvParse csvParseStep;

	private final JsonRecordValidate jsonRecordValidate;

	private final GenerateUrnUUID generateUrnUUID;

	private final DigitalTwinsBatchCsvHandlerUseCase digitalTwinsBatchCsvHandlerUseCase;

	private final EDCBatchHandlerUseCase eDCBatchHandlerUseCase;

	private final StoreBatchCsvHandlerUseCase storeBatchCsvHandlerUseCase;

	private final BatchMapper batchMapper;
	
	private final BatchDeleteService batchDeleteService;

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

		Batch batch = batchMapper.mapFrom(jsonObject);

		digitalTwinsBatchCsvHandlerUseCase.run(batch);

		eDCBatchHandlerUseCase.run(getNameOfModel(), batch, processId);

		storeBatchCsvHandlerUseCase.run(batch);
	}
	

	@Override
	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {
		return batchDeleteService.readCreatedTwinsforDelete(refProcessId);
	}

	@Override
	public void executeDeleteRecord(JsonObject jsonObject, String delProcessId, String refProcessId) {
		batchDeleteService.deleteAllDataBySequence(jsonObject);
	}

	@Override
	public JsonObject readCreatedTwinsDetails(String uuid) {
		return batchDeleteService.readCreatedTwinsDetails(uuid);
	}

}
