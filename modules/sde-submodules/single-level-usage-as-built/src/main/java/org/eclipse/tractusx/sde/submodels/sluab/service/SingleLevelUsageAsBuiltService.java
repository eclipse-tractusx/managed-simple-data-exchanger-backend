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
package org.eclipse.tractusx.sde.submodels.sluab.service;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DeleteDigitalTwinsFacilitator;
import org.eclipse.tractusx.sde.edc.facilitator.DeleteEDCFacilitator;
import org.eclipse.tractusx.sde.submodels.sluab.entity.SingleLevelUsageAsBuiltEntity;
import org.eclipse.tractusx.sde.submodels.sluab.mapper.SingleLevelUsageAsBuiltMapper;
import org.eclipse.tractusx.sde.submodels.sluab.repository.SingleLevelUsageAsBuiltRepository;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Service
@AllArgsConstructor
public class SingleLevelUsageAsBuiltService {
	
	private final SingleLevelUsageAsBuiltRepository singleLevelUsageAsBuiltRepository;

	private final SingleLevelUsageAsBuiltMapper singleLevelUsageAsBuiltMapper;

	private final DeleteEDCFacilitator deleteEDCFacilitator;

	private final DeleteDigitalTwinsFacilitator deleteDigitalTwinsFacilitator;

	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {

		return Optional
				.ofNullable(Optional.ofNullable(singleLevelUsageAsBuiltRepository.findByProcessId(refProcessId))
						.filter(a -> !a.isEmpty())
						.orElseThrow(() -> new NoDataFoundException(
								String.format("No data found for processid %s ", refProcessId)))
						.stream().filter(e -> !CommonConstants.DELETED_Y.equals(e.getDeleted()))
						.map(singleLevelUsageAsBuiltMapper::mapFromEntity).toList())
				.filter(a -> !a.isEmpty()).orElseThrow(
						() -> new NoDataFoundException("No data founds for deletion, All records are already deleted"));
	}

	public void deleteAllDataBySequence(JsonObject jsonObject) {

		SingleLevelUsageAsBuiltEntity aspectRelationshipEntity = singleLevelUsageAsBuiltMapper.mapforEntity(jsonObject);

		deleteEDCAsset(aspectRelationshipEntity);

		deleteDigitalTwinsFacilitator.deleteDigitalTwinsById(aspectRelationshipEntity.getShellId(),aspectRelationshipEntity.getSubModelId());

		saveAspectRelationshipWithDeleted(aspectRelationshipEntity);
	}

	@SneakyThrows
	public void deleteEDCAsset(SingleLevelUsageAsBuiltEntity singleLevelUsageAsBuiltEntity) {

		deleteEDCFacilitator.deleteContractDefination(singleLevelUsageAsBuiltEntity.getContractDefinationId());

		deleteEDCFacilitator.deleteAccessPolicy(singleLevelUsageAsBuiltEntity.getAccessPolicyId());

		deleteEDCFacilitator.deleteUsagePolicy(singleLevelUsageAsBuiltEntity.getUsagePolicyId());

		deleteEDCFacilitator.deleteAssets(singleLevelUsageAsBuiltEntity.getAssetId());
	}

	private void saveAspectRelationshipWithDeleted(SingleLevelUsageAsBuiltEntity singleLevelUsageAsBuiltEntity) {
		singleLevelUsageAsBuiltEntity.setDeleted(CommonConstants.DELETED_Y);
		singleLevelUsageAsBuiltRepository.save(singleLevelUsageAsBuiltEntity);
	}

	public JsonObject readCreatedTwinsDetails(String uuid) {
		List<SingleLevelUsageAsBuiltEntity> entities = Optional
				.ofNullable(singleLevelUsageAsBuiltRepository.findByParentCatenaXId(uuid))
				.orElseThrow(() -> new NoDataFoundException("No data found uuid " + uuid));

		return singleLevelUsageAsBuiltMapper.mapToResponse(uuid, entities);
	}

	public SingleLevelUsageAsBuiltEntity readEntity(String uuid) {
		return Optional.ofNullable(singleLevelUsageAsBuiltRepository.findByChildCatenaXId(uuid))
				.orElseThrow(() -> new NoDataFoundException("No data found uuid " + uuid));
	}

	public int getUpdatedData(String refProcessId) {

		return (int) singleLevelUsageAsBuiltRepository.countByUpdatedAndProcessId(CommonConstants.UPDATED_Y, refProcessId);
	}

}
