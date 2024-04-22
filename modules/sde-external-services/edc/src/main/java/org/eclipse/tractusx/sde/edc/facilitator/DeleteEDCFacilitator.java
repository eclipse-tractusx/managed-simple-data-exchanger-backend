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

package org.eclipse.tractusx.sde.edc.facilitator;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.edc.api.EDCFeignClientApi;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.SneakyThrows;

@Service
public class DeleteEDCFacilitator extends AbstractEDCStepsHelper {

	private final EDCFeignClientApi eDCFeignClientApi;

	public DeleteEDCFacilitator(EDCFeignClientApi eDCFeignClientApi) {
		this.eDCFeignClientApi = eDCFeignClientApi;
	}

	@SneakyThrows
	public void findEDCOfferInformation(String shellId, String subModelId) {
		String assetId = shellId + "-" + subModelId;
		try {
			JsonNode contractDefination = eDCFeignClientApi.getContractDefination(assetId);

			deleteContractDefination(contractDefination.get("@id").asText());
			deleteAccessPolicy(contractDefination.get("edc:accessPolicyId").asText());
			deleteUsagePolicy(contractDefination.get("edc:contractPolicyId").asText());
			deleteAssets(assetId);

		} catch (Exception e) {
			parseExceptionMessage(e);
		}
		
	}

	@SneakyThrows
	public void deleteContractDefination(String contractDefinationId) {
		try {
			eDCFeignClientApi.deleteContractDefinition(contractDefinationId);
		} catch (Exception e) {
			parseExceptionMessage(e);
		}

	}

	@SneakyThrows
	public void deleteAccessPolicy(String accessPolicyId) {
		try {
			eDCFeignClientApi.deletePolicyDefinitions(accessPolicyId);
		} catch (Exception e) {
			parseExceptionMessage(e);
		}

	}

	@SneakyThrows
	public void deleteUsagePolicy(String usagePolicyId) {
		try {
			if (!StringUtils.isBlank(usagePolicyId))
				eDCFeignClientApi.deletePolicyDefinitions(usagePolicyId);
		} catch (Exception e) {
			parseExceptionMessage(e);
		}

	}

	@SneakyThrows
	public void deleteAssets(String assetId) {
		try {
			eDCFeignClientApi.deleteAssets(assetId);
		} catch (Exception e) {
			parseExceptionMessage(e);
		}

	}

	private void parseExceptionMessage(Exception e) throws ServiceException {

		if (!e.toString().contains("FeignException$NotFound") || !e.toString().contains("404 Not Found")) {
			throw new ServiceException("Exception in EDC delete request process:" + e.getMessage());
		}
	}
	
	@SneakyThrows
	public String valueReplacer(String requestTemplate, Map<String, String> inputData) {
		StringSubstitutor stringSubstitutor1 = new StringSubstitutor(inputData);
		return stringSubstitutor1.replace(requestTemplate);
	}
}
