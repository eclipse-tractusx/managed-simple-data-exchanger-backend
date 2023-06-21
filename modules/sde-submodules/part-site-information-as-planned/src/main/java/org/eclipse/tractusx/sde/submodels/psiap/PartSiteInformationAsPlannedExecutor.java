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
package org.eclipse.tractusx.sde.submodels.psiap;

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
import org.eclipse.tractusx.sde.submodels.psiap.mapper.PartSiteInformationAsPlannedMapper;
import org.eclipse.tractusx.sde.submodels.psiap.model.PartSiteInformationAsPlanned;
import org.eclipse.tractusx.sde.submodels.psiap.services.PartSiteInformationAsPlannedService;
import org.eclipse.tractusx.sde.submodels.psiap.steps.DigitalTwinsPartSiteInformationAsPlannedHandlerStep;
import org.eclipse.tractusx.sde.submodels.psiap.steps.EDCPartSiteInformationAsPlannedHandlerStep;
import org.eclipse.tractusx.sde.submodels.psiap.steps.StorePartSiteInformationAsPlannedHandlerStep;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class PartSiteInformationAsPlannedExecutor extends SubmodelExecutor {
	
	private final PartSiteInformationAsPlannedMapper partSiteInformationAsPlannedMapper;

	private final CsvParse csvParseStep;
	
	private final JsonRecordFormating jsonRecordformater;

	private final GenerateUrnUUID generateUrnUUID;

	private final JsonRecordValidate jsonRecordValidate;

	private final DigitalTwinsPartSiteInformationAsPlannedHandlerStep digitalTwinsPartSiteInformationAsPlannedHandlerStep;

	private final EDCPartSiteInformationAsPlannedHandlerStep eDCPartSiteInformationAsPlannedHandlerUseCase;

	private final StorePartSiteInformationAsPlannedHandlerStep storePartSiteInformationAsPlannedCsvHandlerUseCase;

	private final PartSiteInformationAsPlannedService partSiteInformationAsPlannedService;
	
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

		PartSiteInformationAsPlanned partAsPlannedAspect = partSiteInformationAsPlannedMapper.mapFrom(jsonObject);

		digitalTwinsPartSiteInformationAsPlannedHandlerStep.init(getSubmodelSchema());
		digitalTwinsPartSiteInformationAsPlannedHandlerStep.run(partAsPlannedAspect);

		eDCPartSiteInformationAsPlannedHandlerUseCase.init(getSubmodelSchema());
		eDCPartSiteInformationAsPlannedHandlerUseCase.run(getNameOfModel(), partAsPlannedAspect, processId);
		
		if (StringUtils.isBlank(partAsPlannedAspect.getUpdated())) {
			Map<String, String> bpnKeyMap = new HashMap<>();
			bpnKeyMap.put(CommonConstants.MANUFACTURER_PART_ID, partAsPlannedAspect.getManufacturerPartId());
			bPNDiscoveryUseCaseHandler.run(bpnKeyMap);
		}

		storePartSiteInformationAsPlannedCsvHandlerUseCase.run(partAsPlannedAspect);
	}

	@Override
	public void executeDeleteRecord(JsonObject jsonObject, String delProcessId, String refProcessId) {
		partSiteInformationAsPlannedService.deleteAllDataBySequence(jsonObject);
	}

	@Override
	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {
		return partSiteInformationAsPlannedService.readCreatedTwinsforDelete(refProcessId);
	}

	@Override
	public JsonObject readCreatedTwinsDetails(String uuid) {
		return partSiteInformationAsPlannedService.readCreatedTwinsDetails(uuid);
	}

	@Override
	public int getUpdatedRecordCount(String processId) {
		return partSiteInformationAsPlannedService.getUpdatedData(processId);
	}


}
