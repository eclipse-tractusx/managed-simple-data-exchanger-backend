/********************************************************************************
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.sde.submodels.pap.steps;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.enums.UsagePolicyEnum;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequest;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequestFactory;
import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.ContractDefinitionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.ContractDefinitionRequestFactory;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ConstraintRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyConstraintBuilderService;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyDefinitionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyRequestFactory;
import org.eclipse.tractusx.sde.edc.gateways.external.EDCGateway;
import org.eclipse.tractusx.sde.submodels.pap.entity.PartAsPlannedEntity;
import org.eclipse.tractusx.sde.submodels.pap.model.PartAsPlanned;
import org.eclipse.tractusx.sde.submodels.pap.services.PartAsPlannedService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.SneakyThrows;

@Service
public class EDCPartAsPlannedHandlerStep extends Step {

	private final AssetEntryRequestFactory assetFactory;
	private final EDCGateway edcGateway;
	private final PolicyRequestFactory policyFactory;
	private final ContractDefinitionRequestFactory contractFactory;
	private final PolicyConstraintBuilderService policyConstraintBuilderService;
	private final PartAsPlannedService partAsPlannedService;

	public EDCPartAsPlannedHandlerStep(EDCGateway edcGateway, AssetEntryRequestFactory assetFactory,
			PolicyRequestFactory policyFactory, ContractDefinitionRequestFactory contractFactory,
			PolicyConstraintBuilderService policyConstraintBuilderService, PartAsPlannedService partAsPlannedService) {
		this.assetFactory = assetFactory;
		this.edcGateway = edcGateway;
		this.policyFactory = policyFactory;
		this.contractFactory = contractFactory;
		this.policyConstraintBuilderService = policyConstraintBuilderService;
		this.partAsPlannedService = partAsPlannedService;
	}

	@SneakyThrows
	public PartAsPlanned run(String submodel, PartAsPlanned input, String processId) {
		String shellId = input.getShellId();
		String subModelId = input.getSubModelId();

		try {

			AssetEntryRequest assetEntryRequest = assetFactory.getAssetRequest(submodel,
					getSubmodelDescriptionOfModel(), shellId, subModelId, input.getUuid());
			if (!edcGateway.assetExistsLookup(
					assetEntryRequest.getAsset().getProperties().get(CommonConstants.ASSET_PROP_ID))) {
				edcProcessingforPartAsPlanned(assetEntryRequest, input);
			} else {

				deleteEDCFirstForUpdate(submodel, input, processId);
				edcProcessingforPartAsPlanned(assetEntryRequest, input);
				input.setUpdated(CommonConstants.UPDATED_Y);
			}

			return input;
		} catch (Exception e) {
			throw new CsvHandlerUseCaseException(input.getRowNumber(), "EDC: " + e.getMessage());
		}
	}

	@SneakyThrows
	private void deleteEDCFirstForUpdate(String submodel, PartAsPlanned input, String processId) {
		try {
			PartAsPlannedEntity partAsPlannedEntity = partAsPlannedService.readEntity(input.getUuid());
			partAsPlannedService.deleteEDCAsset(partAsPlannedEntity);

		} catch (Exception e) {
			if (!e.getMessage().contains("404 Not Found") && !e.getMessage().contains("No data found")) {
				throw new ServiceException("Exception in EDC delete request process:" + e.getMessage());
			}
		}
	}

	@SneakyThrows
	private void edcProcessingforPartAsPlanned(AssetEntryRequest assetEntryRequest, PartAsPlanned input) {
		HashMap<String, String> extensibleProperties = new HashMap<>();

		String shellId = input.getShellId();
		String subModelId = input.getSubModelId();
		edcGateway.createAsset(assetEntryRequest);

		List<ConstraintRequest> usageConstraints = policyConstraintBuilderService
				.getUsagePolicyConstraints(input.getUsagePolicies());
		List<ConstraintRequest> accessConstraints = policyConstraintBuilderService
				.getAccessConstraints(input.getBpnNumbers());

		String customValue = getCustomValue(input);
		if (StringUtils.isNotBlank(customValue)) {
			extensibleProperties.put(UsagePolicyEnum.CUSTOM.name(), customValue);
		}

		PolicyDefinitionRequest accessPolicyDefinitionRequest = policyFactory.getPolicy(shellId, subModelId,
				accessConstraints, new HashMap<>());
		PolicyDefinitionRequest usagePolicyDefinitionRequest = policyFactory.getPolicy(shellId, subModelId,
				usageConstraints, extensibleProperties);

		edcGateway.createPolicyDefinition(accessPolicyDefinitionRequest);

		edcGateway.createPolicyDefinition(usagePolicyDefinitionRequest);

		ContractDefinitionRequest contractDefinitionRequest = contractFactory.getContractDefinitionRequest(
				assetEntryRequest.getAsset().getProperties().get(CommonConstants.ASSET_PROP_ID),
				accessPolicyDefinitionRequest.getId(), usagePolicyDefinitionRequest.getId());

		edcGateway.createContractDefinition(contractDefinitionRequest);

		// EDC transaction information for DB
		input.setAssetId(assetEntryRequest.getAsset().getProperties().get(CommonConstants.ASSET_PROP_ID));
		input.setAccessPolicyId(accessPolicyDefinitionRequest.getId());
		input.setUsagePolicyId(usagePolicyDefinitionRequest.getId());
		input.setContractDefinationId(contractDefinitionRequest.getId());
	}

	private String getCustomValue(PartAsPlanned input) {
		if (!CollectionUtils.isEmpty(input.getUsagePolicies())) {
			return input.getUsagePolicies().stream().filter(policy -> policy.getType().equals(UsagePolicyEnum.CUSTOM))
					.map(value -> value.getValue()).findFirst().orElse(null);
		}
		return null;
	}

}