/********************************************************************************
 * Copyright (c) 2022, 2024 T-Systems International GmbH
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.bpndiscovery.handler.BPNDiscoveryUseCaseHandler;
import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.entities.csv.RowData;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerDigitalTwinUseCaseException;
import org.eclipse.tractusx.sde.common.submodel.executor.SubmodelExecutor;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.CsvParse;
import org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl.JsonRecordFormating;
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

	private final JsonRecordFormating jsonRecordformater;

	private final SingleLevelBoMASPlannedUUIDUrnUUID generateUrnUUID;

	private final DigitalTwinsSingleLevelBoMAsPlannedHandlerStep digitalTwinsHandlerStep;

	private final EDCSingleLevelBoMAsPlannedHandlerStep eDCHandlerStep;

	private final StoreSingleLevelBoMAsPlannedStep storeSingleLevelBoMAsPlannedStep;

	private final SingleLevelBoMAsPlannedMapper singleLevelBoMAsPlannedMapper;

	private final SingleLevelBoMAsPlannedService singleLevelBoMAsPlannedService;

	private final BPNDiscoveryUseCaseHandler bPNDiscoveryUseCaseHandler;

	@SneakyThrows
	public void executeCsvRecord(RowData rowData, ObjectNode jsonObject, String processId, PolicyModel policy) {

		csvParseStep.init(getSubmodelSchema());
		csvParseStep.run(rowData, jsonObject, processId);

		nextSteps(rowData.position(), jsonObject, processId, policy);
	}

	@SneakyThrows
	public void executeJsonRecord(Integer rowIndex, ObjectNode jsonObject, String processId, PolicyModel policy) {

		jsonRecordformater.init(getSubmodelSchema());
		jsonRecordformater.run(rowIndex, jsonObject, processId);

		nextSteps(rowIndex, jsonObject, processId, policy);

	}

	@SneakyThrows
	private void nextSteps(Integer rowIndex, ObjectNode jsonObject, String processId, PolicyModel policy)
			throws CsvHandlerDigitalTwinUseCaseException {

		SingleLevelBoMAsPlanned singleLevelBoMAsPlanned = singleLevelBoMAsPlannedMapper.mapFrom(jsonObject);

		generateUrnUUID.run(singleLevelBoMAsPlanned, processId);

		jsonObject.put("uuid", singleLevelBoMAsPlanned.getChildUuid());
		jsonObject.put("parent_uuid", singleLevelBoMAsPlanned.getParentUuid());

		jsonRecordValidate.init(getSubmodelSchema());
		jsonRecordValidate.run(rowIndex, jsonObject);

		digitalTwinsHandlerStep.init(getSubmodelSchema());
		digitalTwinsHandlerStep.run(singleLevelBoMAsPlanned, policy);

		eDCHandlerStep.init(getSubmodelSchema());
		eDCHandlerStep.run(getNameOfModel(), singleLevelBoMAsPlanned, processId, policy);

		if (StringUtils.isBlank(singleLevelBoMAsPlanned.getUpdated())) {
			Map<String, String> bpnKeyMap = new HashMap<>();
			bpnKeyMap.put(CommonConstants.MANUFACTURER_PART_ID, singleLevelBoMAsPlanned.getChildManufacturerPartId());
			bPNDiscoveryUseCaseHandler.run(bpnKeyMap);
		}

		storeSingleLevelBoMAsPlannedStep.run(singleLevelBoMAsPlanned);
	}

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

	@Override
	public List<JsonObject> readCreatedTwinsByProcessId(String refProcessId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void executeDeleteRecord(Integer rowIndex, JsonObject jsonObject, String delProcessId, String refProcessId) {
		// TODO Auto-generated method stub
		
	}

}