/********************************************************************************
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
import org.eclipse.tractusx.sde.submodels.pap.steps.DigitalTwinsPartAsPlannedHandlerStep;
import org.eclipse.tractusx.sde.submodels.pap.steps.EDCPartAsPlannedHandlerStep;
import org.eclipse.tractusx.sde.submodels.pap.steps.StorePartAsPlannedHandlerStep;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;
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

	private final DigitalTwinsPartAsPlannedHandlerStep digitalTwinsPartAsPlannedCsvHandlerUseCase;

	private final EDCPartAsPlannedHandlerStep eDCPartAsPlannedHandlerUseCase;

	private final StorePartAsPlannedHandlerStep storePartAsPlannedCsvHandlerUseCase;

	private final PartAsPlannedService partAsPlannedService;

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

	@Override
	public int getUpdatedRecordCount(String processId) {
		return partAsPlannedService.getUpdatedData(processId);
	}

}
