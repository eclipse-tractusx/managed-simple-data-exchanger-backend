/********************************************************************************
 * Copyright (c) 2023 T-Systems International GmbH
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.sde.submodels.pcf;

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
import org.eclipse.tractusx.sde.submodels.pcf.mapper.PcfMapper;
import org.eclipse.tractusx.sde.submodels.pcf.model.PcfAspect;
import org.eclipse.tractusx.sde.submodels.pcf.service.PcfService;
import org.eclipse.tractusx.sde.submodels.pcf.steps.DigitalTwinsPcfCsvHandlerUseCase;
import org.eclipse.tractusx.sde.submodels.pcf.steps.EDCPcfHandlerUseCase;
import org.eclipse.tractusx.sde.submodels.pcf.steps.StorePcfCsvHandlerUseCase;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Component
@AllArgsConstructor
public class PcfExecutor extends SubmodelExecutor {
	
	private final PcfMapper pcfMapperforPcf;

	private final CsvParse csvParseStepforPcf;

	private final JsonRecordFormating jsonRecordformaterforPcf;

	private final GenerateUrnUUID generateUrnUUIDforPcf;

	private final JsonRecordValidate jsonRecordValidateforPcf;

	private final DigitalTwinsPcfCsvHandlerUseCase digitalTwinsAspectCsvHandlerUseCaseforPcf;

	private final EDCPcfHandlerUseCase eDCAspectHandlerUseCaseforPcf;

	private final StorePcfCsvHandlerUseCase storeAspectCsvHandlerUseCaseforPcf;
	
	private final BPNDiscoveryUseCaseHandler bPNDiscoveryUseCaseHandlerforPcf; 

	private final PcfService aspectServiceforPcf;

	@SneakyThrows
	public void executeCsvRecord(RowData rowData, ObjectNode jsonObject, String processId) {

		csvParseStepforPcf.init(getSubmodelSchema());
		csvParseStepforPcf.run(rowData, jsonObject, processId);

		nextStepsforPcf(rowData.position(), jsonObject, processId);

	}

	@SneakyThrows
	public void executeJsonRecord(Integer rowIndex, ObjectNode jsonObject, String processId) {

		jsonRecordformaterforPcf.init(getSubmodelSchema());
		jsonRecordformaterforPcf.run(rowIndex, jsonObject, processId);

		nextStepsforPcf(rowIndex, jsonObject, processId);

	}

	@SneakyThrows
	private void nextStepsforPcf(Integer rowIndex, ObjectNode jsonObject, String processId)
			throws CsvHandlerDigitalTwinUseCaseException {

		//Setting uuid for global asset id use
		jsonObject.put("uuid", jsonObject.get("id").asText());
		//setting this fields for digital twin shell short id generation
		jsonObject.put("manufacturer_part_id", jsonObject.get("productId").asText());
		jsonObject.put("name_at_manufacturer", jsonObject.get("companyName").asText());
		
		generateUrnUUIDforPcf.run(jsonObject, processId);

		jsonRecordValidateforPcf.init(getSubmodelSchema());
		jsonRecordValidateforPcf.run(rowIndex, jsonObject);

		PcfAspect pcfAspect = pcfMapperforPcf.mapFrom(jsonObject);

		
		
		digitalTwinsAspectCsvHandlerUseCaseforPcf.init(getSubmodelSchema());
		digitalTwinsAspectCsvHandlerUseCaseforPcf.run(pcfAspect);

		eDCAspectHandlerUseCaseforPcf.init(getSubmodelSchema());
		eDCAspectHandlerUseCaseforPcf.run(getNameOfModel(), pcfAspect, processId);
		
		if (StringUtils.isBlank(pcfAspect.getUpdatedforPcf())) {
			Map<String, String> bpnKeyMap = new HashMap<>();
			bpnKeyMap.put(CommonConstants.MANUFACTURER_PART_ID, pcfAspect.getProductId());
			bPNDiscoveryUseCaseHandlerforPcf.run(bpnKeyMap);
		}

		storeAspectCsvHandlerUseCaseforPcf.run(pcfAspect);
	}

	@Override
	public void executeDeleteRecord(JsonObject jsonObject, String delProcessId, String refProcessId) {
		aspectServiceforPcf.deleteAllDataBySequence(jsonObject);
	}

	@Override
	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {
		return aspectServiceforPcf.readCreatedTwinsforDelete(refProcessId);
	}

	@Override
	public JsonObject readCreatedTwinsDetails(String uuid) {
		return aspectServiceforPcf.readCreatedTwinsDetails(uuid);
	}

	@Override
	public int getUpdatedRecordCount(String processId) {
		return aspectServiceforPcf.getUpdatedData(processId);
	}


}
