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

package org.eclipse.tractusx.sde.edc.facilitator;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.entities.Policies;
import org.eclipse.tractusx.sde.edc.api.ContractApi;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ActionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ConstraintRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PermissionRequest;
import org.eclipse.tractusx.sde.edc.enums.NegotiationState;
import org.eclipse.tractusx.sde.edc.mapper.ContractMapper;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.AcknowledgementId;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractAgreementDto;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractAgreementInfo;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractAgreementResponse;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractNegotiationDto;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractNegotiations;
import org.eclipse.tractusx.sde.edc.util.UtilityFunctions;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class ContractNegotiateManagementHelper extends AbstractEDCStepsHelper {

	private final ContractApi contractApi;
	private final ContractMapper contractMapper;

	ObjectMapper mapper = new ObjectMapper();

	@SneakyThrows
	public String negotiateContract(String providerUrl, String providerId, String offerId, String assetId,
			List<ActionRequest> action, Map<String, String> extensibleProperty) {

		var recipientURL = UtilityFunctions.removeLastSlashOfUrl(providerUrl);
		if (!recipientURL.endsWith(protocolPath))
			recipientURL = recipientURL + protocolPath;

		ContractNegotiations contractNegotiations = contractMapper.prepareContractNegotiations(recipientURL, offerId,
				assetId, providerId, action);

		AcknowledgementId acknowledgementId = contractApi.contractnegotiations(new URI(consumerHost),
				contractNegotiations, getAuthHeader());
		return acknowledgementId.getId();
	}

	@SneakyThrows
	public ContractNegotiationDto checkContractNegotiationStatus(String negotiateContractId) {

		return contractApi.getContractDetails(new URI(consumerHost), negotiateContractId, getAuthHeader());

	}

	@SneakyThrows
	public List<ContractNegotiationDto> getAllContractNegotiations(String type, Integer limit, Integer offset) {

		if (UtilityFunctions.checkTypeOfConnector(type)) {
			return contractApi.getAllContractNegotiations(new URI(providerHost), getProviderAuthHeader());
		} else
			return contractApi.getAllContractNegotiations(new URI(consumerHost), getAuthHeader());

	}

	@SneakyThrows
	public List<JsonNode> getAllContractAgreements(String assetId, String type, Integer offset, Integer limit) {

		Map<String, String> inputMap = new HashMap<>();
		inputMap.put("operandLeft", "assetId");
		inputMap.put("operandRight", assetId);
		inputMap.put("offset", offset + "");
		inputMap.put("limit", limit + "");

		JsonNode body = getQurySpec(inputMap);

		if (UtilityFunctions.checkTypeOfConnector(type)) {
			return contractApi.getAllContractAgreements(new URI(providerHost), getProviderAuthHeader(), body);
		} else
			return contractApi.getAllContractAgreements(new URI(consumerHost), getAuthHeader(), body);
	}

	@SneakyThrows
	public ContractNegotiationDto checkContractAgreementNegotiationStatus(String contractAgreement) {
		return contractApi.getContractAgreementsNegotiation(new URI(consumerHost), contractAgreement, getAuthHeader());
	}

	@SneakyThrows
	public List<JsonNode> getAllTransfer(String assetId, String type, Integer offset, Integer limit) {
		Map<String, String> inputMap = new HashMap<>();
		inputMap.put("operandLeft", "dataRequest.assetId");
		inputMap.put("operandRight", assetId);
		inputMap.put("offset", offset + "");
		inputMap.put("limit", limit + "");

		JsonNode body = getQurySpec(inputMap);

		if (UtilityFunctions.checkTypeOfConnector(type)) {
			return contractApi.getAllTransfer(new URI(providerHost), getProviderAuthHeader(), body);
		} else
			return contractApi.getAllTransfer(new URI(consumerHost), getAuthHeader(), body);
	}

	@SneakyThrows
	private JsonNode getQurySpec(Map<String, String> inputMap) {

		String querySpec = """
						{
						    "@context": {
						        "edc": "https://w3id.org/edc/v0.0.1/ns/"
						    },
						    "@type": "QuerySpec",
						    "offset": ${offset},
						    "limit": ${limit},
						    "sortOrder": "DESC",
						    "filterExpression": [
						        {
						            "operandLeft": "${operandLeft}",
						            "operator": "=",
						            "operandRight": "${operandRight}"
						        }
						    ]
						}
				""";
		return mapper.readTree(UtilityFunctions.valueReplacer(querySpec, inputMap));
	}

	@SuppressWarnings("unchecked")
	@SneakyThrows
	public ContractAgreementResponse getAgreementBasedOnNegotiationId(String type, String negotiationId) {
		ContractAgreementResponse agreementResponse = null;
		ContractAgreementDto agreement = null;
		ObjectMapper objeMapper = new ObjectMapper();

		if (UtilityFunctions.checkTypeOfConnector(type)) {
			agreement = contractApi.getAgreementBasedOnNegotiationId(new URI(providerHost), negotiationId,
					getProviderAuthHeader());
		} else {
			agreement = contractApi.getAgreementBasedOnNegotiationId(new URI(consumerHost), negotiationId,
					getAuthHeader());
		}
		if (agreement != null) {
			List<Policies> policies = new ArrayList<>();
			Object permissionObj = agreement.getPolicy().getPermissions();

			if (permissionObj instanceof ArrayList) {
				for (Object obj : (ArrayList<Object>) permissionObj)
					formatPermissionConstraint(objeMapper, policies, obj);
			} else if (permissionObj != null) {
				formatPermissionConstraint(objeMapper, policies, permissionObj);
			}

			if (policies.isEmpty())
			
			UtilityFunctions.getUsagePolicies(policies, List.of());
			ContractAgreementInfo agreementInfo = ContractAgreementInfo.builder()
					.contractEndDate(agreement.getContractEndDate())
					.contractSigningDate(agreement.getContractSigningDate())
					.contractStartDate(agreement.getContractStartDate()).assetId(agreement.getAssetId())
					.policies(policies).build();

			agreementResponse = ContractAgreementResponse.builder().contractAgreementId(agreement.getId())
					.organizationName(StringUtils.EMPTY).title(StringUtils.EMPTY).negotiationId(negotiationId)
					.contractAgreementInfo(agreementInfo).build();

		}

		return agreementResponse;
	}

	private void formatPermissionConstraint(ObjectMapper objeMapper, List<Policies> policies, Object permissionObj) {
		ObjectMapper objMapper = new ObjectMapper();
		PermissionRequest permissionRequest = objMapper.convertValue(permissionObj, PermissionRequest.class);

		Object object = permissionRequest.getConstraint().get("odrl:and");
		if (object != null)
			setContraint(objeMapper, policies, object);
		else {
			object = permissionRequest.getConstraint().get("odrl:or");
			if (object != null)
				setContraint(objeMapper, policies, object);
		}
	}

	private void setContraint(ObjectMapper objeMapper, List<Policies> policies, Object object) {
		if (object instanceof ArrayList) {
			List<ConstraintRequest> convertValue = objeMapper.convertValue(object,
					new TypeReference<List<ConstraintRequest>>() {
					});
			UtilityFunctions.getUsagePolicies(policies, convertValue);
		} else if (object != null) {

			ConstraintRequest convertValue = objeMapper.convertValue(object, ConstraintRequest.class);
			UtilityFunctions.getUsagePolicies(policies, List.of(convertValue));
		}
	}

	public Map<String, Object> getAllContractOffers(String type, Integer limit, Integer offset) {
		List<ContractAgreementResponse> contractAgreementResponses = new ArrayList<>();
		List<ContractNegotiationDto> contractNegotiationDtoList = getAllContractNegotiations(type, limit, offset);

		contractNegotiationDtoList.stream().filter(contract -> type.equalsIgnoreCase(contract.getType().name()))
				.forEach(contract -> {
					if (StringUtils.isNotBlank(contract.getContractAgreementId())
							&& (contract.getState().equals(NegotiationState.FINALIZED.name())
									|| (NegotiationState.DECLINED.name().equalsIgnoreCase(contract.getErrorDetail())
											&& contract.getState().equals(NegotiationState.TERMINATED.name())))) {
						String negotiationId = contract.getId();
						ContractAgreementResponse agreementResponse = getAgreementBasedOnNegotiationId(type,
								negotiationId);
						agreementResponse.setCounterPartyAddress(contract.getCounterPartyAddress());
						agreementResponse.setDateCreated(contract.getCreatedAt());
						agreementResponse.setDateUpdated(contract.getUpdatedAt());
						agreementResponse.setType(contract.getType());
						agreementResponse.setState(contract.getState());
						agreementResponse.setErrorDetail(contract.getErrorDetail());
						contractAgreementResponses.add(agreementResponse);
					} else {
						ContractAgreementResponse agreementResponse = ContractAgreementResponse.builder()
								.contractAgreementId(StringUtils.EMPTY).organizationName(StringUtils.EMPTY)
								.title(StringUtils.EMPTY).negotiationId(contract.getId()).state(contract.getState())
								.contractAgreementInfo(null).counterPartyAddress(contract.getCounterPartyAddress())
								.type(contract.getType()).dateCreated(contract.getCreatedAt())
								.dateUpdated(contract.getUpdatedAt()).errorDetail(contract.getErrorDetail()).build();
						contractAgreementResponses.add(agreementResponse);
					}
				});

		Map<String, Object> res = new HashMap<>();
		if (UtilityFunctions.checkTypeOfConnector(type))
			res.put("connector", providerHostWithoutDataPath);
		else
			res.put("connector", consumerHostWithoutDataPath);

		res.put("contracts", contractAgreementResponses);
		return res;
	}

	@SneakyThrows
	public void declineContract(String type, String negotiationId) {

		if (UtilityFunctions.checkTypeOfConnector(type)) {
			contractApi.declineContract(new URI(providerHost), negotiationId, getProviderAuthHeader());
		} else {
			contractApi.declineContract(new URI(consumerHost), negotiationId, getAuthHeader());
		}
	}

	@SneakyThrows
	public void cancelContract(String type, String negotiationId) {

		if (UtilityFunctions.checkTypeOfConnector(type)) {
			contractApi.cancelContract(new URI(providerHost), negotiationId, getProviderAuthHeader());
		} else {
			contractApi.cancelContract(new URI(consumerHost), negotiationId, getAuthHeader());
		}
	}

}