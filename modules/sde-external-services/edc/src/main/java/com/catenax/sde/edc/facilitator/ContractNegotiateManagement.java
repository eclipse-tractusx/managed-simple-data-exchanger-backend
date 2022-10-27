/********************************************************************************
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the CatenaX (ng) GitHub Organisation
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

package com.catenax.sde.edc.facilitator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.catenax.sde.edc.api.ContractApi;
import com.catenax.sde.edc.entities.UsagePolicy;
import com.catenax.sde.edc.entities.request.policies.ConstraintRequest;
import com.catenax.sde.edc.enums.NegotiationState;
import com.catenax.sde.edc.mapper.ContractMapper;
import com.catenax.sde.edc.model.contractnegotiation.AcknowledgementId;
import com.catenax.sde.edc.model.contractnegotiation.ContractAgreementDto;
import com.catenax.sde.edc.model.contractnegotiation.ContractAgreementInfo;
import com.catenax.sde.edc.model.contractnegotiation.ContractAgreementResponse;
import com.catenax.sde.edc.model.contractnegotiation.ContractNegotiationDto;
import com.catenax.sde.edc.model.contractnegotiation.ContractNegotiations;
import com.catenax.sde.edc.model.contractnegotiation.ContractNegotiationsResponse;
import com.catenax.sde.edc.util.UtilityFunctions;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class ContractNegotiateManagement extends AbstractEDCStepsHelper {

    private final ContractApi contractApi;
    private final ContractMapper contractMapper;

    @SneakyThrows
    public String negotiateContract(String offerId, String provider, String assetId, List<ConstraintRequest> constraintRequests, Map<String, String> extensibleProperty) {

        ContractNegotiations contractNegotiations = contractMapper.prepareContractNegotiations(offerId,
                assetId, provider, constraintRequests);
        contractNegotiations.getOffer().getPolicy().setExtensibleProperties(extensibleProperty);

        AcknowledgementId acknowledgementId = contractApi.contractnegotiations(contractNegotiations,
                getAuthHeader());
        return acknowledgementId.getId();
    }

    @SneakyThrows
    public ContractNegotiationsResponse checkContractNegotiationStatus(String negotiateContractId) {

        return contractApi.checkContractNegotiationsStatus(negotiateContractId, getAuthHeader());

    }

    @SneakyThrows
    public List<ContractNegotiationDto> getAllContractNegotiations(Integer limit, Integer offset) {

        return contractApi.getAllContractNegotiations(limit, offset, getAuthHeader());

    }

    @SneakyThrows
    public ContractAgreementResponse getAgreementBasedOnNegotiationId(String negotiationId) {
        ContractAgreementResponse agreementResponse = null;
        ContractAgreementDto agreement = contractApi.getAgreementBasedOnNegotiationId(negotiationId, getAuthHeader());
        if(agreement != null) {
            List<UsagePolicy> policies = new ArrayList<>();
            agreement.getPolicy().getPermissions().stream().forEach(permission -> {
                policies.addAll(UtilityFunctions.getUsagePolicies(permission.getConstraints().stream()));
            });
            UtilityFunctions.addCustomUsagePolicy(agreement.getPolicy().getExtensibleProperties(), policies);
            ContractAgreementInfo agreementInfo = ContractAgreementInfo.builder().contractEndDate(agreement.getContractEndDate())
                    .contractSigningDate(agreement.getContractSigningDate()).contractStartDate(agreement.getContractStartDate())
                    .assetId(agreement.getAssetId()).policies(policies).build();
            agreementResponse = ContractAgreementResponse.builder().contractAgreementId(agreement.getId()).organizationName(StringUtils.EMPTY)
                    .title(StringUtils.EMPTY).negotiationId(negotiationId).state(NegotiationState.CONFIRMED.name())
                    .contractAgreementInfo(agreementInfo).build();

        }
        return agreementResponse;
    }
}
