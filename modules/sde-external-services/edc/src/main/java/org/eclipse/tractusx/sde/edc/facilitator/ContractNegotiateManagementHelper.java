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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.edc.api.ContractApi;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ActionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ConstraintRequest;
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
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class ContractNegotiateManagementHelper extends AbstractEDCStepsHelper {

	private final ContractApi contractApi;
	private final ContractMapper contractMapper;

	@SneakyThrows
	public String negotiateContract(String providerUrl, String providerId, String offerId, String assetId,
			ActionRequest action, Map<String, String> extensibleProperty) {

		ContractNegotiations contractNegotiations = contractMapper
				.prepareContractNegotiations(providerUrl + protocolPath, offerId, assetId, providerId, action);

		// it looks extensible property not supporting
		// contractNegotiations.getOffer().getPolicy().setExtensibleProperties(extensibleProperty);

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
			List<UsagePolicies> policies = new ArrayList<>();

			Object object = agreement.getPolicy().getPermissions().getConstraint().get("odrl:and");

			if (object instanceof ArrayList) {
				List<ConstraintRequest> convertValue = objeMapper.convertValue(object,
						new TypeReference<List<ConstraintRequest>>() {
						});
				policies.addAll(UtilityFunctions.getUsagePolicies(convertValue));
			} else {
				
				ConstraintRequest convertValue = objeMapper.convertValue(object, ConstraintRequest.class);
				policies.addAll(UtilityFunctions.getUsagePolicies(List.of(convertValue)));
			}

			UtilityFunctions.addCustomUsagePolicy(agreement.getPolicy().getExtensibleProperties(), policies);

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

	public Map<String, Object> getAllContractOffers(String type, Integer limit, Integer offset) {
		List<ContractAgreementResponse> contractAgreementResponses = new ArrayList<>();
		List<ContractNegotiationDto> contractNegotiationDtoList = getAllContractNegotiations(type, limit, offset);

		contractNegotiationDtoList.stream().forEach((contract) -> {
			if (StringUtils.isBlank(type) || contract.getType().name().equals(type)) {
				if (contract.getState().equals(NegotiationState.FINALIZED.name())
						|| contract.getState().equals(NegotiationState.DECLINED.name())) {
					String negotiationId = contract.getId();
					if (StringUtils.isNotBlank(contract.getContractAgreementId())) {
						ContractAgreementResponse agreementResponse = getAgreementBasedOnNegotiationId(type,
								negotiationId);
						agreementResponse.setCounterPartyAddress(contract.getCounterPartyAddress());
						agreementResponse.setDateCreated(contract.getCreatedAt());
						agreementResponse.setDateUpdated(contract.getUpdatedAt());
						agreementResponse.setType(contract.getType());
						agreementResponse.setState(contract.getState());
						contractAgreementResponses.add(agreementResponse);
					}
				} else {
					ContractAgreementResponse agreementResponse = ContractAgreementResponse.builder()
							.contractAgreementId(StringUtils.EMPTY).organizationName(StringUtils.EMPTY)
							.title(StringUtils.EMPTY).negotiationId(contract.getId()).state(contract.getState())
							.contractAgreementInfo(null).counterPartyAddress(contract.getCounterPartyAddress())
							.type(contract.getType()).dateCreated(contract.getCreatedAt())
							.dateUpdated(contract.getUpdatedAt()).build();
					contractAgreementResponses.add(agreementResponse);
				}
			}
		});

		Map<String, Object> res = new HashMap<>();
		if (UtilityFunctions.checkTypeOfConnector(type))
			res.put("connector", providerHost);
		else
			res.put("connector", consumerHost);

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
