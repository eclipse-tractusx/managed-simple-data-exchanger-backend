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

package org.eclipse.tractusx.sde.submodels.batch.steps;

import java.util.Map;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequest;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequestFactory;
import org.eclipse.tractusx.sde.edc.facilitator.CreateEDCAssetFacilator;
import org.eclipse.tractusx.sde.edc.facilitator.DeleteEDCFacilitator;
import org.eclipse.tractusx.sde.edc.gateways.external.EDCGateway;
import org.eclipse.tractusx.sde.submodels.batch.entity.BatchEntity;
import org.eclipse.tractusx.sde.submodels.batch.model.Batch;
import org.eclipse.tractusx.sde.submodels.batch.service.BatchService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EDCBatchHandlerUseCase extends Step {

	private final AssetEntryRequestFactory assetFactory;
	private final EDCGateway edcGateway;
	private final CreateEDCAssetFacilator createEDCAssetFacilator;
	private final BatchService batchDeleteService;
	private final DeleteEDCFacilitator deleteEDCFacilitator;

	@SneakyThrows
	public Batch run(String submodel, Batch input, String processId, PolicyModel policy) {
		String shellId = input.getShellId();
		String subModelId = input.getSubModelId();

		try {

			AssetEntryRequest assetEntryRequest = assetFactory.getAssetRequest(submodel,
					getSubmodelShortDescriptionOfModel(), shellId, subModelId, input.getUuid());

			Map<String, String> eDCAsset = null;

			if (!edcGateway.assetExistsLookup(assetEntryRequest.getId())) {
				eDCAsset = createEDCAssetFacilator.createEDCAsset(assetEntryRequest, policy);
			} else {
				eDCAsset = createEDCAssetFacilator.updateEDCAsset(assetEntryRequest, policy);
				input.setUpdated(CommonConstants.UPDATED_Y);
			}

			// EDC transaction information for DB
			input.setAssetId(eDCAsset.get("assetId"));
			input.setAccessPolicyId(eDCAsset.get("accessPolicyId"));
			input.setUsagePolicyId(eDCAsset.get("usagePolicyId"));
			input.setContractDefinationId(eDCAsset.get("contractDefinitionId"));

			return input;
		} catch (Exception e) {
			throw new CsvHandlerUseCaseException(input.getRowNumber(), "EDC: " + e.getMessage());
		}
	}

	@SneakyThrows
	private void deleteEDCFirstForUpdate(String submodel, Batch input, String processId) {
		try {
			BatchEntity batchEntity = batchDeleteService.readEntity(input.getUuid());
			batchDeleteService.deleteEDCAsset(batchEntity);
		} catch (NoDataFoundException e) {
			log.warn(
					"The EDC assetInfo not found in local database for delete, looking EDC connector and going to delete asset");
			deleteEDCFacilitator.findEDCOfferInformation(input.getShellId(), input.getSubModelId());
		} catch (Exception e) {
			if (!e.getMessage().contains("404 Not Found")) {
				throw new ServiceException("Unable to delete EDC offer for update: " + e.getMessage());
			}
		}
	}

	@SneakyThrows
	private void edcProcessingforBatch(AssetEntryRequest assetEntryRequest, Batch input, PolicyModel policy) {

		Map<String, String> createEDCAsset = createEDCAssetFacilator.createEDCAsset(assetEntryRequest, policy);

		// EDC transaction information for DB
		input.setAssetId(assetEntryRequest.getId());
		input.setAccessPolicyId(createEDCAsset.get("accessPolicyId"));
		input.setUsagePolicyId(createEDCAsset.get("usagePolicyId"));
		input.setContractDefinationId(createEDCAsset.get("contractDefinitionId"));
	}
}
