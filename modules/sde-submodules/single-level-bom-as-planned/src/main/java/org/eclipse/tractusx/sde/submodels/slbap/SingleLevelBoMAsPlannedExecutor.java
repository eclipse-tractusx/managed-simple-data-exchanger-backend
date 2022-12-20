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
package org.eclipse.tractusx.sde.submodels.slbap;

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.csv.RowData;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerDigitalTwinUseCaseException;
import org.eclipse.tractusx.sde.common.submodel.executor.SubmodelExecutor;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.CsvParse;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.JsonRecordValidate;
import org.eclipse.tractusx.sde.submodels.slbap.mapper.SingleLevelBoMAsPlannedMapper;
import org.eclipse.tractusx.sde.submodels.slbap.model.SingleLevelBoMAsPlanned;
import org.eclipse.tractusx.sde.submodels.slbap.services.SingleLevelBoMAsPlannedService;
import org.eclipse.tractusx.sde.submodels.slbap.steps.DigitalTwinsSingleLevelBoMAsPlannedHandlerStep;
import org.eclipse.tractusx.sde.submodels.slbap.steps.EDCSingleLevelBoMAsPlannedHandlerStep;
import org.eclipse.tractusx.sde.submodels.slbap.steps.SingleLevelBoMASPlannedUUIDUrnUUID;
import org.eclipse.tractusx.sde.submodels.slbap.steps.StoreSingleLevelBoMAsPlannedStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Component
@AllArgsConstructor
public class SingleLevelBoMAsPlannedExecutor extends SubmodelExecutor {

	@Autowired
	private final CsvParse csvParseStep;
	
	private final JsonRecordValidate jsonRecordValidate;

	private final SingleLevelBoMASPlannedUUIDUrnUUID generateUrnUUID;
	
	private final DigitalTwinsSingleLevelBoMAsPlannedHandlerStep digitalTwinsHandlerStep;

	private final EDCSingleLevelBoMAsPlannedHandlerStep eDCHandlerStep;

	private final StoreSingleLevelBoMAsPlannedStep storeSingleLevelBoMAsPlannedStep;
	
	private final SingleLevelBoMAsPlannedMapper singleLevelBoMAsPlannedMapper;

	private final SingleLevelBoMAsPlannedService singleLevelBoMAsPlannedService;

	
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
	
	private void nextSteps(Integer rowIndex, ObjectNode jsonObject, String processId) throws CsvHandlerDigitalTwinUseCaseException {

		SingleLevelBoMAsPlanned singleLevelBoMAsPlanned = singleLevelBoMAsPlannedMapper.mapFrom(jsonObject);

		generateUrnUUID.run(singleLevelBoMAsPlanned, processId);
		
		jsonObject.put("uuid",singleLevelBoMAsPlanned.getChildUuid());
		jsonObject.put("parent_uuid",singleLevelBoMAsPlanned.getParentUuid());
		
		jsonRecordValidate.init(getSubmodelSchema());
		jsonRecordValidate.run(rowIndex, jsonObject);

		digitalTwinsHandlerStep.init(getSubmodelSchema());
		digitalTwinsHandlerStep.run(singleLevelBoMAsPlanned);

		eDCHandlerStep.init(getSubmodelSchema());
		eDCHandlerStep.run(getNameOfModel(), singleLevelBoMAsPlanned, processId);

		storeSingleLevelBoMAsPlannedStep.run(singleLevelBoMAsPlanned);
	}

	@Override
	public void executeDeleteRecord(JsonObject jsonObject, String delProcessId, String refProcessId) {
		singleLevelBoMAsPlannedService.deleteAllDataBySequence(jsonObject);
	}

	@Override
	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {
		return singleLevelBoMAsPlannedService.readCreatedTwinsforDelete(refProcessId);
	}

	@Override
	public JsonObject readCreatedTwinsDetails(String uuid) {
		return singleLevelBoMAsPlannedService.readCreatedTwinsDetails(uuid);
	}

	@Override
	public int getUpdatedRecordCount(String processId) {
		return singleLevelBoMAsPlannedService.getUpdatedData(processId);
	}


}
