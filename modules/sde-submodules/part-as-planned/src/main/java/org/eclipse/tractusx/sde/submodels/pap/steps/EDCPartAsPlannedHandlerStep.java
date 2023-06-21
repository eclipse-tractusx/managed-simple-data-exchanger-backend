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
package org.eclipse.tractusx.sde.submodels.pap.steps;

import java.util.Map;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequest;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequestFactory;
import org.eclipse.tractusx.sde.edc.facilitator.CreateEDCAssetFacilator;
import org.eclipse.tractusx.sde.edc.gateways.external.EDCGateway;
import org.eclipse.tractusx.sde.submodels.pap.entity.PartAsPlannedEntity;
import org.eclipse.tractusx.sde.submodels.pap.model.PartAsPlanned;
import org.eclipse.tractusx.sde.submodels.pap.services.PartAsPlannedService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class EDCPartAsPlannedHandlerStep extends Step {

	private final AssetEntryRequestFactory assetFactory;
	private final EDCGateway edcGateway;
	private final CreateEDCAssetFacilator createEDCAssetFacilator;
	private final PartAsPlannedService partAsPlannedService;

	@SneakyThrows
	public PartAsPlanned run(String submodel, PartAsPlanned input, String processId) {
		String shellId = input.getShellId();
		String subModelId = input.getSubModelId();

		try {

			AssetEntryRequest assetEntryRequest = assetFactory.getAssetRequest(submodel,
					getSubmodelShortDescriptionOfModel(), shellId, subModelId, input.getUuid());
			if (!edcGateway.assetExistsLookup(
					assetEntryRequest.getAsset().getId())) {
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
			if (!e.getMessage().contains("404 Not Found")) {
				throw new ServiceException("Unable to delete EDC offer for update: " + e.getMessage());
			}
		}
	}

	@SneakyThrows
	private void edcProcessingforPartAsPlanned(AssetEntryRequest assetEntryRequest, PartAsPlanned input) {

		Map<String, String> createEDCAsset = createEDCAssetFacilator.createEDCAsset(assetEntryRequest,
				input.getBpnNumbers(), input.getUsagePolicies());

		// EDC transaction information for DB
		input.setAssetId(assetEntryRequest.getAsset().getId());
		input.setAccessPolicyId(createEDCAsset.get("accessPolicyId"));
		input.setUsagePolicyId(createEDCAsset.get("usagePolicyId"));
		input.setContractDefinationId(createEDCAsset.get("contractDefinitionId"));
	}

}