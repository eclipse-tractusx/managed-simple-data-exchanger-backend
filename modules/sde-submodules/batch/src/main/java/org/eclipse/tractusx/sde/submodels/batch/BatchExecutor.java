/********************************************************************************
 * Copyright (c) 2022, 2023 T-Systems International GmbH
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.sde.submodels.batch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.bpndiscovery.handler.BPNDiscoveryUseCaseHandler;
import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.entities.csv.RowData;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerDigitalTwinUseCaseException;
import org.eclipse.tractusx.sde.common.submodel.executor.SubmodelExecutor;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.CsvParse;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.GenerateUrnUUID;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.JsonRecordFormating;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.JsonRecordValidate;
import org.eclipse.tractusx.sde.submodels.batch.mapper.BatchMapper;
import org.eclipse.tractusx.sde.submodels.batch.model.Batch;
import org.eclipse.tractusx.sde.submodels.batch.service.BatchService;
import org.eclipse.tractusx.sde.submodels.batch.steps.DigitalTwinsBatchCsvHandlerUseCase;
import org.eclipse.tractusx.sde.submodels.batch.steps.EDCBatchHandlerUseCase;
import org.eclipse.tractusx.sde.submodels.batch.steps.StoreBatchCsvHandlerUseCase;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Component
@AllArgsConstructor
public class BatchExecutor extends SubmodelExecutor {

	private final CsvParse csvParseStep;
	
	private final JsonRecordFormating jsonRecordformater;

	private final JsonRecordValidate jsonRecordValidate;

	private final GenerateUrnUUID generateUrnUUID;

	private final DigitalTwinsBatchCsvHandlerUseCase digitalTwinsBatchCsvHandlerUseCase;

	private final EDCBatchHandlerUseCase eDCBatchHandlerUseCase;

	private final StoreBatchCsvHandlerUseCase storeBatchCsvHandlerUseCase;

	private final BatchMapper batchMapper;

	private final BatchService batchDeleteService;
	
	private final BPNDiscoveryUseCaseHandler bPNDiscoveryUseCaseHandler; 

	@SneakyThrows
	public void executeCsvRecord(RowData rowData, ObjectNode jsonObject, String processId) {

		csvParseStep.init(getSubmodelSchema());
		csvParseStep.run(rowData, jsonObject, processId);

		nextSteps(rowData.position(), jsonObject, processId);

	}

	@SneakyThrows
	public void executeJsonRecord(Integer rowIndex, ObjectNode jsonObject, String processId) {

		jsonRecordformater.init(getSubmodelSchema());
		jsonRecordformater.run(rowIndex, jsonObject, processId);
		
		nextSteps(rowIndex, jsonObject, processId);

	}

	private void nextSteps(Integer rowIndex, ObjectNode jsonObject, String processId)
			throws CsvHandlerDigitalTwinUseCaseException {
		generateUrnUUID.run(jsonObject, processId);

		jsonRecordValidate.init(getSubmodelSchema());
		jsonRecordValidate.run(rowIndex, jsonObject);

		Batch batch = batchMapper.mapFrom(jsonObject);

		digitalTwinsBatchCsvHandlerUseCase.init(getSubmodelSchema());
		digitalTwinsBatchCsvHandlerUseCase.run(batch);

		eDCBatchHandlerUseCase.init(getSubmodelSchema());
		eDCBatchHandlerUseCase.run(getNameOfModel(), batch, processId);
		
		if (StringUtils.isBlank(batch.getUpdated())) {
			Map<String, String> bpnKeyMap = new HashMap<>();
			bpnKeyMap.put(CommonConstants.MANUFACTURER_PART_ID, batch.getManufacturerPartId());
			bPNDiscoveryUseCaseHandler.run(bpnKeyMap);
		}

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

	@Override
	public int getUpdatedRecordCount(String processId) {
		return batchDeleteService.getUpdatedData(processId);
	}

}
