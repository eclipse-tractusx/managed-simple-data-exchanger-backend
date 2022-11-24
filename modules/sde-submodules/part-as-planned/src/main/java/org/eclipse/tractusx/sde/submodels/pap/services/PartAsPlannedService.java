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
package org.eclipse.tractusx.sde.submodels.pap.services;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DeleteDigitalTwinsFacilitator;
import org.eclipse.tractusx.sde.edc.facilitator.DeleteEDCFacilitator;
import org.eclipse.tractusx.sde.submodels.pap.constants.PartAsPlannedConstants;
import org.eclipse.tractusx.sde.submodels.pap.entity.PartAsPlannedEntity;
import org.eclipse.tractusx.sde.submodels.pap.mapper.PartAsPlannedMapper;
import org.eclipse.tractusx.sde.submodels.pap.repository.PartAsPlannedRepository;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PartAsPlannedService {

	private final PartAsPlannedRepository partAsPlannedRepository;

	private final PartAsPlannedMapper partAsPlannedMapper;

	private final DeleteEDCFacilitator deleteEDCFacilitator;

	private final DeleteDigitalTwinsFacilitator deleteDigitalTwinsFacilitator;

	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {

		return Optional
				.ofNullable(Optional.ofNullable(partAsPlannedRepository.findByProcessId(refProcessId))
						.filter(a -> !a.isEmpty())
						.orElseThrow(() -> new NoDataFoundException(
								String.format("No data found for processid %s ", refProcessId)))
						.stream().filter(e -> !PartAsPlannedConstants.DELETED_Y.equals(e.getDeleted()))
						.map(partAsPlannedMapper::mapFromEntity).toList())
				.filter(a -> !a.isEmpty()).orElseThrow(
						() -> new NoDataFoundException("No data founds for deletion, All records are already deleted"));

	}

	public void deleteAllDataBySequence(JsonObject jsonObject) {

		PartAsPlannedEntity partAsPlannedEntity = partAsPlannedMapper.mapforEntity(jsonObject);

		deleteDigitalTwinsFacilitator.deleteDigitalTwinsById(partAsPlannedEntity.getShellId());

		deleteEDCAsset(partAsPlannedEntity);
		saveAspectWithDeleted(partAsPlannedEntity);
	}

	public void deleteEDCAsset(PartAsPlannedEntity partAsPlannedEntity) {

		deleteEDCFacilitator.deleteContractDefination(partAsPlannedEntity.getContractDefinationId());

		deleteEDCFacilitator.deleteAccessPolicy(partAsPlannedEntity.getAccessPolicyId());

		deleteEDCFacilitator.deleteUsagePolicy(partAsPlannedEntity.getUsagePolicyId());

		deleteEDCFacilitator.deleteAssets(partAsPlannedEntity.getAssetId());
	}

	private void saveAspectWithDeleted(PartAsPlannedEntity aspectEntity) {

		aspectEntity.setDeleted(PartAsPlannedConstants.DELETED_Y);
		partAsPlannedRepository.save(aspectEntity);
	}

	public JsonObject readCreatedTwinsDetails(String uuid) {
		return partAsPlannedMapper.mapToResponse(readEntity(uuid));
	}

	public int getUpdatedData(String refProcessId) {

		return (int) partAsPlannedRepository.countByUpdatedAndProcessId(CommonConstants.UPDATED_Y, refProcessId);
	}

	public PartAsPlannedEntity readEntity(String uuid) {
		return Optional.ofNullable(partAsPlannedRepository.findByUuid(uuid))
				.orElseThrow(() -> new NoDataFoundException("No data found uuid " + uuid));
	}

}
