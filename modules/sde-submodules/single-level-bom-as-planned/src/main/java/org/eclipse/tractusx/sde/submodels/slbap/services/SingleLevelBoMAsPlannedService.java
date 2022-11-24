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
package org.eclipse.tractusx.sde.submodels.slbap.services;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DeleteDigitalTwinsFacilitator;
import org.eclipse.tractusx.sde.edc.facilitator.DeleteEDCFacilitator;
import org.eclipse.tractusx.sde.submodels.pap.constants.PartAsPlannedConstants;
import org.eclipse.tractusx.sde.submodels.slbap.entity.SingleLevelBoMAsPlannedEntity;
import org.eclipse.tractusx.sde.submodels.slbap.mapper.SingleLevelBoMAsPlannedMapper;
import org.eclipse.tractusx.sde.submodels.slbap.repository.SingleLevelBoMAsPlannedRepository;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SingleLevelBoMAsPlannedService {

	private final SingleLevelBoMAsPlannedRepository singleLevelBoMAsPlannedRepository;

	private final SingleLevelBoMAsPlannedMapper singleLevelBoMAsPlannedMapper;

	private final DeleteEDCFacilitator deleteEDCFacilitator;

	private final DeleteDigitalTwinsFacilitator deleteDigitalTwinsFacilitator;

	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {

		return Optional
				.ofNullable(Optional.ofNullable(singleLevelBoMAsPlannedRepository.findByProcessId(refProcessId))
						.filter(a -> !a.isEmpty())
						.orElseThrow(() -> new NoDataFoundException(
								String.format("No data found for processid %s ", refProcessId)))
						.stream().filter(e -> !PartAsPlannedConstants.DELETED_Y.equals(e.getDeleted()))
						.map(singleLevelBoMAsPlannedMapper::mapFromEntity).toList())
				.filter(a -> !a.isEmpty()).orElseThrow(
						() -> new NoDataFoundException("No data founds for deletion, All records are already deleted"));
	}

	public void deleteAllDataBySequence(JsonObject jsonObject) {

		SingleLevelBoMAsPlannedEntity singleLevelBoMAsPlannedEntity = singleLevelBoMAsPlannedMapper
				.mapforEntity(jsonObject);

		deleteDigitalTwinsFacilitator.deleteDigitalTwinsById(singleLevelBoMAsPlannedEntity.getShellId());

		deleteEDCAsset(singleLevelBoMAsPlannedEntity);

		saveSingleLevelBoMAsPlannedWithDeleted(singleLevelBoMAsPlannedEntity);
	}

	private void saveSingleLevelBoMAsPlannedWithDeleted(SingleLevelBoMAsPlannedEntity aspectRelationshipEntity) {
		aspectRelationshipEntity.setDeleted(PartAsPlannedConstants.DELETED_Y);
		singleLevelBoMAsPlannedRepository.save(aspectRelationshipEntity);
	}

	public JsonObject readCreatedTwinsDetails(String uuid) {
		List<SingleLevelBoMAsPlannedEntity> entities = Optional
				.ofNullable(singleLevelBoMAsPlannedRepository.findByParentCatenaXId(uuid))
				.orElseThrow(() -> new NoDataFoundException("No data found uuid " + uuid));
		return singleLevelBoMAsPlannedMapper.mapToResponse(uuid, entities);
	}

	public SingleLevelBoMAsPlannedEntity readEntity(String uuid) {
		return Optional.ofNullable(singleLevelBoMAsPlannedRepository.findByChildCatenaXId(uuid))
						.orElseThrow(() -> new NoDataFoundException("No data found uuid " + uuid));
	}

	public void deleteEDCAsset(SingleLevelBoMAsPlannedEntity singleLevelBoMAsPlannedEntity) {

		deleteEDCFacilitator.deleteContractDefination(singleLevelBoMAsPlannedEntity.getContractDefinationId());

		deleteEDCFacilitator.deleteAccessPolicy(singleLevelBoMAsPlannedEntity.getAccessPolicyId());

		deleteEDCFacilitator.deleteUsagePolicy(singleLevelBoMAsPlannedEntity.getUsagePolicyId());

		deleteEDCFacilitator.deleteAssets(singleLevelBoMAsPlannedEntity.getAssetId());
	}

	public int getUpdatedData(String refProcessId) {

		return (int) singleLevelBoMAsPlannedRepository.countByUpdatedAndProcessId(CommonConstants.UPDATED_Y,
				refProcessId);
	}

}
