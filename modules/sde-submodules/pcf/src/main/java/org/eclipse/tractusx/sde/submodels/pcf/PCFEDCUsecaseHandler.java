/********************************************************************************
 * Copyright (c) 2024 T-Systems International GmbH
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

import org.eclipse.tractusx.sde.common.configuration.properties.PCFAssetStaticPropertyHolder;
import org.eclipse.tractusx.sde.common.constants.SubmoduleCommonColumnsConstant;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.submodel.executor.EDCUsecaseStep;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.common.utils.JsonObjectUtility;
import org.eclipse.tractusx.sde.common.utils.PolicyOperationUtil;
import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.ContractDefinitionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.ContractDefinitionRequestFactory;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyConstraintBuilderService;
import org.eclipse.tractusx.sde.edc.gateways.external.EDCGateway;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("pcfEDCUsecaseHandler")
@RequiredArgsConstructor
public class PCFEDCUsecaseHandler extends Step implements EDCUsecaseStep {

    private final PCFAssetStaticPropertyHolder pcfAssetStaticPropertyHolder;

    private final PolicyConstraintBuilderService policyConstraintBuilderService;

    private final ContractDefinitionRequestFactory contractFactory;

    private final EDCGateway edcGateway;

    @SneakyThrows
    @Override
    public ObjectNode run(Integer rowNumber, ObjectNode objectNode, String processId, PolicyModel policy) {

        Map<String, String> output = new HashMap<>();

        String shellId = JsonObjectUtility.getValueFromJsonObjectAsString(objectNode,
                SubmoduleCommonColumnsConstant.SHELL_ID);

        String subModelId = JsonObjectUtility.getValueFromJsonObjectAsString(objectNode,
                SubmoduleCommonColumnsConstant.SUBMODULE_ID);

        String newOfferId = shellId + "-" + subModelId;

        // Get existing pcf id
        String assetId = pcfAssetStaticPropertyHolder.getPcfExchangeAssetId();

        // API call to added BPN business partner group
        List<String> sharedBPNList = PolicyOperationUtil.getAccessBPNList(policy);

        // Create access policy
        JsonNode accessPolicyDefinitionRequest = policyConstraintBuilderService.getAccessPolicy(newOfferId, assetId, policy);
        String accessPolicyUUId = accessPolicyDefinitionRequest.get("@id").asText();
        if (!edcGateway.policyExistsLookup(accessPolicyUUId)) {
            edcGateway.createPolicyDefinition(accessPolicyDefinitionRequest);
        } else{
            // Update existing policy
            edcGateway.updatePolicyDefinition(accessPolicyUUId, accessPolicyDefinitionRequest);
        }

        // Create usage policy
        JsonNode usagePolicyDefinitionRequest = policyConstraintBuilderService.getUsagePolicy(newOfferId, assetId, policy);
        String usagePolicyUUId = usagePolicyDefinitionRequest.get("@id").asText();
        if (!edcGateway.policyExistsLookup(usagePolicyUUId)) {
            edcGateway.createPolicyDefinition(usagePolicyDefinitionRequest);
        } else {
            // Update existing policy
            edcGateway.updatePolicyDefinition(usagePolicyUUId, usagePolicyDefinitionRequest);
        }

        // Create contract definition
        ContractDefinitionRequest contractDefinitionRequest = contractFactory
                .getContractDefinitionRequest(newOfferId, assetId, accessPolicyUUId, usagePolicyUUId);

        String contractDefinitionId = contractDefinitionRequest.getId();
        if (!edcGateway.contractDefinitionExistsLookup(contractDefinitionId)) {
            edcGateway.createContractDefinition(contractDefinitionRequest);
        } else {
            // Update existing contract definition
            edcGateway.updateContractDefinition(contractDefinitionRequest);
        }

        for (String bpnNumber : sharedBPNList) {
            //get call
            edcGateway.deleteBPNfromPCFBusinessPartnerGroup(bpnNumber, pcfAssetStaticPropertyHolder.getPcfBusinessPartnerGroup());
        }

        output.put(SubmoduleCommonColumnsConstant.ASSET_ID, assetId);
        output.put(SubmoduleCommonColumnsConstant.ACCESS_POLICY_ID, accessPolicyUUId);
        output.put(SubmoduleCommonColumnsConstant.USAGE_POLICY_ID, usagePolicyUUId);
        output.put(SubmoduleCommonColumnsConstant.CONTRACT_DEFINATION_ID, contractDefinitionId);

        output.entrySet().forEach(entry -> objectNode.put(entry.getKey(), entry.getValue()));

        // existing policy access and usage update

        return objectNode;

    }

    @Override
    public void delete(Integer rowIndex, JsonObject jsonObject, String delProcessId, String refProcessId) {
        log.warn("No need to delete EDC asset for PCF exchange");

    }
}