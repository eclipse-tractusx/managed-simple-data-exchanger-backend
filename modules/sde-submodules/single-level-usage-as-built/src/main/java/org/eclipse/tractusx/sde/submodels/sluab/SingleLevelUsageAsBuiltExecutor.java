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

		SingleLevelUsageAsBuilt singleLevelUsageAsBuilt = singleLevelUsageAsBuiltMapper.mapFrom(jsonObject);

//		generateUrnUUID.run(singleLevelUsageAsBuilt, processId);

		jsonObject.put("uuid", singleLevelUsageAsBuilt.getChildUuid());
		jsonObject.put("parent_uuid", singleLevelUsageAsBuilt.getParentUuid());

		jsonRecordValidate.init(getSubmodelSchema());
		jsonRecordValidate.run(rowIndex, jsonObject);

		digitalTwinsSingleLevelUsageAsBuiltCsvHandlerUseCase.init(getSubmodelSchema());
		digitalTwinsSingleLevelUsageAsBuiltCsvHandlerUseCase.run(singleLevelUsageAsBuilt, policy);

		eDCSingleLevelUsageAsBuiltHandlerUseCase.init(getSubmodelSchema());
		eDCSingleLevelUsageAsBuiltHandlerUseCase.run(getNameOfModel(), singleLevelUsageAsBuilt, processId, policy);

		if (StringUtils.isBlank(singleLevelUsageAsBuilt.getUpdated())) {
			Map<String, String> bpnKeyMap = new HashMap<>();
			bpnKeyMap.put(CommonConstants.MANUFACTURER_PART_ID, singleLevelUsageAsBuilt.getChildManufacturerPartId());
			bPNDiscoveryUseCaseHandler.run(bpnKeyMap);
		}

		storeSingleLevelUsageAsBuiltCsvHandlerUseCase.run(singleLevelUsageAsBuilt);
	}

	@Override
	public void executeDeleteRecord(Integer rowIndex, JsonObject jsonObject, String delProcessId, String refProcessId) {
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
