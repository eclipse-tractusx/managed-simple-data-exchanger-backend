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

package org.eclipse.tractusx.sde.submodels.apr.service;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DeleteDigitalTwinsFacilitator;
import org.eclipse.tractusx.sde.edc.facilitator.DeleteEDCFacilitator;
import org.eclipse.tractusx.sde.submodels.apr.entity.AspectRelationshipEntity;
import org.eclipse.tractusx.sde.submodels.apr.mapper.AspectRelationshipMapper;
import org.eclipse.tractusx.sde.submodels.apr.repository.AspectRelationshipRepository;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Service
@AllArgsConstructor
public class AspectRelationshipService {

	private final AspectRelationshipRepository aspectRelationshipRepository;

	private final AspectRelationshipMapper aspectRelationshipMapper;

	public static final String DELETED_Y = "Y";

	private final DeleteEDCFacilitator deleteEDCFacilitator;

	private final DeleteDigitalTwinsFacilitator deleteDigitalTwinsFacilitator;

	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {

		return Optional
				.ofNullable(Optional.ofNullable(aspectRelationshipRepository.findByProcessId(refProcessId))
						.filter(a -> !a.isEmpty())
						.orElseThrow(() -> new NoDataFoundException(
								String.format("No data found for processid %s ", refProcessId)))
						.stream().filter(e -> !DELETED_Y.equals(e.getDeleted()))
						.map(aspectRelationshipMapper::mapFromEntity).toList())
				.filter(a -> !a.isEmpty()).orElseThrow(
						() -> new NoDataFoundException("No data founds for deletion, All records are already deleted"));
	}

	public void deleteAllDataBySequence(JsonObject jsonObject) {

		AspectRelationshipEntity aspectRelationshipEntity = aspectRelationshipMapper.mapforEntity(jsonObject);

		deleteEDCAsset(aspectRelationshipEntity);

		deleteDigitalTwinsFacilitator.deleteDigitalTwinsById(aspectRelationshipEntity.getShellId(),aspectRelationshipEntity.getSubModelId());

		saveAspectRelationshipWithDeleted(aspectRelationshipEntity);
	}

	@SneakyThrows
	public void deleteEDCAsset(AspectRelationshipEntity aspectRelationshipEntity) {

		deleteEDCFacilitator.deleteContractDefination(aspectRelationshipEntity.getContractDefinationId());

		deleteEDCFacilitator.deleteAccessPolicy(aspectRelationshipEntity.getAccessPolicyId());

		deleteEDCFacilitator.deleteUsagePolicy(aspectRelationshipEntity.getUsagePolicyId());

		deleteEDCFacilitator.deleteAssets(aspectRelationshipEntity.getAssetId());
	}

	private void saveAspectRelationshipWithDeleted(AspectRelationshipEntity aspectRelationshipEntity) {
		aspectRelationshipEntity.setDeleted(DELETED_Y);
		aspectRelationshipRepository.save(aspectRelationshipEntity);
	}

	public JsonObject readCreatedTwinsDetails(String uuid) {
		List<AspectRelationshipEntity> entities = Optional
				.ofNullable(aspectRelationshipRepository.findByParentCatenaXId(uuid))
				.orElseThrow(() -> new NoDataFoundException("No data found uuid " + uuid));

		return aspectRelationshipMapper.mapToResponse(uuid, entities);
	}

	public AspectRelationshipEntity readEntity(String uuid) {
		return Optional.ofNullable(aspectRelationshipRepository.findByChildCatenaXId(uuid))
				.orElseThrow(() -> new NoDataFoundException("No data found uuid " + uuid));
	}

	public int getUpdatedData(String refProcessId) {

		return (int) aspectRelationshipRepository.countByUpdatedAndProcessId(CommonConstants.UPDATED_Y, refProcessId);
	}

}
