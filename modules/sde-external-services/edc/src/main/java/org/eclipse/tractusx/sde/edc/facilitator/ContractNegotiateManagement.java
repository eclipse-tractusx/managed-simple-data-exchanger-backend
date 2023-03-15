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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.edc.api.ContractApi;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ConstraintRequest;
import org.eclipse.tractusx.sde.edc.mapper.ContractMapper;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.AcknowledgementId;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractAgreementDto;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractAgreementInfo;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractAgreementResponse;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractNegotiationDto;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractNegotiations;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractNegotiationsResponse;
import org.eclipse.tractusx.sde.edc.util.UtilityFunctions;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class ContractNegotiateManagement extends AbstractEDCStepsHelper {

	private final ContractApi contractApi;
	private final ContractMapper contractMapper;

	@SneakyThrows
	public String negotiateContract(String offerId, String provider, String assetId,
			List<ConstraintRequest> constraintRequests, Map<String, String> extensibleProperty) {

		ContractNegotiations contractNegotiations = contractMapper.prepareContractNegotiations(offerId, assetId,
				provider, constraintRequests);
		contractNegotiations.getOffer().getPolicy().setExtensibleProperties(extensibleProperty);

		AcknowledgementId acknowledgementId = contractApi.contractnegotiations(new URI(consumerHost),
				contractNegotiations, getAuthHeader());
		return acknowledgementId.getId();
	}

	@SneakyThrows
	public ContractNegotiationsResponse checkContractNegotiationStatus(String negotiateContractId) {

		return contractApi.checkContractNegotiationsStatus(new URI(consumerHost), negotiateContractId, getAuthHeader());

	}

	@SneakyThrows
	public List<ContractNegotiationDto> getAllContractNegotiations(String type, Integer limit, Integer offset) {
		if (UtilityFunctions.checkTypeOfConnector(type))
			return contractApi.getAllContractNegotiations(new URI(providerHost), limit, offset,
					getProviderAuthHeader());
		else
			return contractApi.getAllContractNegotiations(new URI(consumerHost), limit, offset, getAuthHeader());

	}

	@SneakyThrows
	public ContractAgreementResponse getAgreementBasedOnNegotiationId(String type, String negotiationId) {
		ContractAgreementResponse agreementResponse = null;
		ContractAgreementDto agreement = null;

		if (UtilityFunctions.checkTypeOfConnector(type)) {
			agreement = contractApi.getAgreementBasedOnNegotiationId(new URI(providerHost), negotiationId,
					getProviderAuthHeader());
		} else {
			agreement = contractApi.getAgreementBasedOnNegotiationId(new URI(consumerHost), negotiationId,
					getAuthHeader());
		}

		if (agreement != null) {
			List<UsagePolicies> policies = new ArrayList<>();
			agreement.getPolicy().getPermissions().stream().forEach(permission -> {
				policies.addAll(UtilityFunctions.getUsagePolicies(permission.getConstraints().stream()));
			});
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
}
