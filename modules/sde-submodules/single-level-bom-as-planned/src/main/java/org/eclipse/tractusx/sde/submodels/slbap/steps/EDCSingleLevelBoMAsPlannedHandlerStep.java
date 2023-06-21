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
package org.eclipse.tractusx.sde.submodels.slbap.steps;

import static org.eclipse.tractusx.sde.common.constants.CommonConstants.ASSET_PROP_ID;

import java.util.Map;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequest;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequestFactory;
import org.eclipse.tractusx.sde.edc.facilitator.CreateEDCAssetFacilator;
import org.eclipse.tractusx.sde.edc.gateways.external.EDCGateway;
import org.eclipse.tractusx.sde.submodels.slbap.entity.SingleLevelBoMAsPlannedEntity;
import org.eclipse.tractusx.sde.submodels.slbap.model.SingleLevelBoMAsPlanned;
import org.eclipse.tractusx.sde.submodels.slbap.services.SingleLevelBoMAsPlannedService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class EDCSingleLevelBoMAsPlannedHandlerStep extends Step {

	private final AssetEntryRequestFactory assetFactory;
	private final EDCGateway edcGateway;
	private final CreateEDCAssetFacilator createEDCAssetFacilator;
	private final SingleLevelBoMAsPlannedService singleLevelBoMAsPlannedService;

	@SneakyThrows
	public SingleLevelBoMAsPlanned run(String submodel, SingleLevelBoMAsPlanned input, String processId) {

		String shellId = input.getShellId();
		String subModelId = input.getSubModelId();

		try {
			AssetEntryRequest assetEntryRequest = assetFactory.getAssetRequest(submodel,
					getSubmodelShortDescriptionOfModel(), shellId, subModelId, input.getParentUuid());
			if (!edcGateway.assetExistsLookup(assetEntryRequest.getAsset().getProperties().get(ASSET_PROP_ID))) {
				edcProcessingforSingleLevelBoMAsPlanned(assetEntryRequest, input);
			} else {
				deleteEDCFirstForUpdate(submodel, input, processId);
				edcProcessingforSingleLevelBoMAsPlanned(assetEntryRequest, input);
				input.setUpdated(CommonConstants.UPDATED_Y);
			}

			return input;
		} catch (Exception e) {
			throw new CsvHandlerUseCaseException(input.getRowNumber(), "EDC: " + e.getMessage());
		}
	}

	@SneakyThrows
	private void deleteEDCFirstForUpdate(String submodel, SingleLevelBoMAsPlanned input, String processId) {
		try {
			SingleLevelBoMAsPlannedEntity singleLevelBoMAsPlannedEntity = singleLevelBoMAsPlannedService
					.readEntity(input.getChildUuid());
			singleLevelBoMAsPlannedService.deleteEDCAsset(singleLevelBoMAsPlannedEntity);

		} catch (Exception e) {
			if (!e.getMessage().contains("404 Not Found")) {
				throw new ServiceException("Unable to delete EDC offer for update: " + e.getMessage());
			}
		}
	}

	@SneakyThrows
	private void edcProcessingforSingleLevelBoMAsPlanned(AssetEntryRequest assetEntryRequest,
			SingleLevelBoMAsPlanned input) {

		Map<String, String> createEDCAsset = createEDCAssetFacilator.createEDCAsset(assetEntryRequest,
				input.getBpnNumbers(), input.getUsagePolicies());

		// EDC transaction information for DB
		input.setAssetId(assetEntryRequest.getAsset().getId());
		input.setAccessPolicyId(createEDCAsset.get("accessPolicyId"));
		input.setUsagePolicyId(createEDCAsset.get("usagePolicyId"));
		input.setContractDefinationId(createEDCAsset.get("contractDefinitionId"));
	}
}