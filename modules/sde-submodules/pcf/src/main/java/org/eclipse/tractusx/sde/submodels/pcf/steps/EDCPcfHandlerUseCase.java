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

package org.eclipse.tractusx.sde.submodels.pcf.steps;

import java.util.Map;

import org.eclipse.tractusx.sde.common.constants.SubmoduleCommonColumnsConstant;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.submodel.executor.EDCUsecaseStep;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.common.utils.JsonObjectUtility;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequest;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequestFactory;
import org.eclipse.tractusx.sde.edc.facilitator.CreateEDCAssetFacilator;
import org.eclipse.tractusx.sde.edc.gateways.external.EDCGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class EDCPcfHandlerUseCase extends Step implements EDCUsecaseStep {

	private final AssetEntryRequestFactory assetFactory;
	private final EDCGateway edcGateway;
	private final CreateEDCAssetFacilator createEDCAssetFacilator;

	@Value(value = "${dft.hostname}")
	private String dftHostname;

	@SneakyThrows
	@Override
	public JsonNode run(Integer rowIndex, ObjectNode jsonObject, String processId, PolicyModel policy) {

		try {
			String submodule = getNameOfModel();
			String shellId = JsonObjectUtility.getValueFromJsonObjectAsString(jsonObject,
					SubmoduleCommonColumnsConstant.SHELL_ID);
			String subModelId = JsonObjectUtility.getValueFromJsonObjectAsString(jsonObject,
					SubmoduleCommonColumnsConstant.SUBMODULE_ID);
			String uuid = JsonObjectUtility.getValueFromJsonObjectAsString(jsonObject, getIdentifierOfModel());

			AssetEntryRequest assetEntryRequest = assetFactory.getAssetRequest(submodule,
					getSubmodelShortDescriptionOfModel(), shellId, subModelId, uuid, getsemanticIdOfModel(), "");

			String baseURL = UriComponentsBuilder.fromHttpUrl(dftHostname).path("/pcf/productIds/")
					.path(uuid).toUriString();

			assetEntryRequest.getDataAddress().getProperties().put("baseUrl", baseURL);

			Map<String, String> eDCAsset = null;

			if (!edcGateway.assetExistsLookup(assetEntryRequest.getId())) {
				eDCAsset = createEDCAssetFacilator.createEDCAsset(assetEntryRequest, policy);
			} else {
				eDCAsset = createEDCAssetFacilator.updateEDCAsset(assetEntryRequest, policy);
			}
			eDCAsset.entrySet().forEach(entry -> jsonObject.put(entry.getKey(), entry.getValue()));
			return jsonObject;
		} catch (Exception e) {
			throw new CsvHandlerUseCaseException(rowIndex, "EDC: " + e.getMessage());
		}
	}

	@Override
	public void delete(Integer rowIndex, JsonObject jsonObject, String delProcessId, String refProcessId) {

	}

}