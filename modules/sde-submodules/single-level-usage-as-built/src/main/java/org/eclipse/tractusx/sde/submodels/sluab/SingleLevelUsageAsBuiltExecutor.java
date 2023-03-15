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
package org.eclipse.tractusx.sde.submodels.sluab;

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.csv.RowData;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerDigitalTwinUseCaseException;
import org.eclipse.tractusx.sde.common.submodel.executor.SubmodelExecutor;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.CsvParse;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.JsonRecordFormating;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.JsonRecordValidate;
import org.eclipse.tractusx.sde.submodels.sluab.mapper.SingleLevelUsageAsBuiltMapper;
import org.eclipse.tractusx.sde.submodels.sluab.model.SingleLevelUsageAsBuilt;
import org.eclipse.tractusx.sde.submodels.sluab.service.SingleLevelUsageAsBuiltService;
import org.eclipse.tractusx.sde.submodels.sluab.steps.DigitalTwinsSingleLevelUsageAsBuiltCsvHandlerUseCase;
import org.eclipse.tractusx.sde.submodels.sluab.steps.EDCSingleLevelUsageAsBuiltHandlerUseCase;
import org.eclipse.tractusx.sde.submodels.sluab.steps.SingleLevelUsageAsBuiltUUIDUrnUUID;
import org.eclipse.tractusx.sde.submodels.sluab.steps.StoreSingleLevelUsageAsBuiltCsvHandlerUseCase;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class SingleLevelUsageAsBuiltExecutor extends SubmodelExecutor {

	private final CsvParse csvParseStep;

	private final JsonRecordValidate jsonRecordValidate;

	private final JsonRecordFormating jsonRecordformater;
	
	private final SingleLevelUsageAsBuiltUUIDUrnUUID generateUrnUUID;

	private final DigitalTwinsSingleLevelUsageAsBuiltCsvHandlerUseCase digitalTwinsSingleLevelUsageAsBuiltCsvHandlerUseCase;

	private final EDCSingleLevelUsageAsBuiltHandlerUseCase eDCSingleLevelUsageAsBuiltHandlerUseCase;

	private final StoreSingleLevelUsageAsBuiltCsvHandlerUseCase storeSingleLevelUsageAsBuiltCsvHandlerUseCase;

	private final SingleLevelUsageAsBuiltMapper singleLevelUsageAsBuiltMapper;

	private final SingleLevelUsageAsBuiltService singleLevelUsageAsBuiltService;

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

		SingleLevelUsageAsBuilt singleLevelUsageAsBuilt = singleLevelUsageAsBuiltMapper.mapFrom(jsonObject);

		generateUrnUUID.run(singleLevelUsageAsBuilt, processId);

		jsonObject.put("uuid", singleLevelUsageAsBuilt.getChildUuid());
		jsonObject.put("parent_uuid", singleLevelUsageAsBuilt.getParentUuid());

		jsonRecordValidate.init(getSubmodelSchema());
		jsonRecordValidate.run(rowIndex, jsonObject);

		digitalTwinsSingleLevelUsageAsBuiltCsvHandlerUseCase.init(getSubmodelSchema());
		digitalTwinsSingleLevelUsageAsBuiltCsvHandlerUseCase.run(singleLevelUsageAsBuilt);

		eDCSingleLevelUsageAsBuiltHandlerUseCase.init(getSubmodelSchema());
		eDCSingleLevelUsageAsBuiltHandlerUseCase.run(getNameOfModel(), singleLevelUsageAsBuilt, processId);

		storeSingleLevelUsageAsBuiltCsvHandlerUseCase.run(singleLevelUsageAsBuilt);
	}

	@Override
	public void executeDeleteRecord(JsonObject jsonObject, String delProcessId, String refProcessId) {
		singleLevelUsageAsBuiltService.deleteAllDataBySequence(jsonObject);
	}

	@Override
	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {
		return singleLevelUsageAsBuiltService.readCreatedTwinsforDelete(refProcessId);
	}

	@Override
	public JsonObject readCreatedTwinsDetails(String uuid) {
		return singleLevelUsageAsBuiltService.readCreatedTwinsDetails(uuid);
	}

	@Override
	public int getUpdatedRecordCount(String processId) {
		return singleLevelUsageAsBuiltService.getUpdatedData(processId);
	}

}
