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
package org.eclipse.tractusx.sde.submodels.psiap.services;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DeleteDigitalTwinsFacilitator;
import org.eclipse.tractusx.sde.edc.facilitator.DeleteEDCFacilitator;
import org.eclipse.tractusx.sde.submodels.psiap.constants.PartSiteInformationAsPlannedConstants;
import org.eclipse.tractusx.sde.submodels.psiap.entity.PartSiteInformationAsPlannedEntity;
import org.eclipse.tractusx.sde.submodels.psiap.mapper.PartSiteInformationAsPlannedMapper;
import org.eclipse.tractusx.sde.submodels.psiap.repository.PartSiteInformationAsPlannedRepository;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PartSiteInformationAsPlannedService {
	private final PartSiteInformationAsPlannedRepository partSiteInformationAsPlannedRepository;

	private final PartSiteInformationAsPlannedMapper partSiteInformationAsPlannedMapper;

	private final DeleteEDCFacilitator deleteEDCFacilitator;

	private final DeleteDigitalTwinsFacilitator deleteDigitalTwinsFacilitator;

	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {

		return Optional
				.ofNullable(Optional.ofNullable(partSiteInformationAsPlannedRepository.findByProcessId(refProcessId))
						.filter(a -> !a.isEmpty())
						.orElseThrow(() -> new NoDataFoundException(
								String.format("No data found for processid %s ", refProcessId)))
						.stream().filter(e -> !PartSiteInformationAsPlannedConstants.DELETED_Y.equals(e.getDeleted()))
						.map(partSiteInformationAsPlannedMapper::mapFromEntity).toList())
				.filter(a -> !a.isEmpty()).orElseThrow(
						() -> new NoDataFoundException("No data founds for deletion, All records are already deleted"));

	}

	public void deleteAllDataBySequence(JsonObject jsonObject) {

		PartSiteInformationAsPlannedEntity partSiteInformationAsPlannedEntity = partSiteInformationAsPlannedMapper
				.mapforEntity(jsonObject);

		deleteEDCAsset(partSiteInformationAsPlannedEntity);

		deleteDigitalTwinsFacilitator.deleteDigitalTwinsById(partSiteInformationAsPlannedEntity.getShellId(),
				partSiteInformationAsPlannedEntity.getSubModelId());

		saveAspectWithDeleted(partSiteInformationAsPlannedEntity);
	}

	public void deleteEDCAsset(PartSiteInformationAsPlannedEntity partSiteInformationAsPlannedEntity) {

		deleteEDCFacilitator.deleteContractDefination(partSiteInformationAsPlannedEntity.getContractDefinationId());

		deleteEDCFacilitator.deleteAccessPolicy(partSiteInformationAsPlannedEntity.getAccessPolicyId());

		deleteEDCFacilitator.deleteUsagePolicy(partSiteInformationAsPlannedEntity.getUsagePolicyId());

		deleteEDCFacilitator.deleteAssets(partSiteInformationAsPlannedEntity.getAssetId());
	}

	private void saveAspectWithDeleted(PartSiteInformationAsPlannedEntity aspectEntity) {

		aspectEntity.setDeleted(PartSiteInformationAsPlannedConstants.DELETED_Y);
		partSiteInformationAsPlannedRepository.save(aspectEntity);
	}

	public JsonObject readCreatedTwinsDetails(String uuid) {
		List<PartSiteInformationAsPlannedEntity> entities = Optional
				.ofNullable(partSiteInformationAsPlannedRepository.findAllByUuid(uuid))
				.orElseThrow(() -> new NoDataFoundException("No data found uuid " + uuid));

		return partSiteInformationAsPlannedMapper.mapToResponse(uuid, entities);
	}

	public int getUpdatedData(String refProcessId) {

		return (int) partSiteInformationAsPlannedRepository.countByUpdatedAndProcessId(CommonConstants.UPDATED_Y,
				refProcessId);
	}

	public PartSiteInformationAsPlannedEntity readEntity(String uuid) {
		return Optional.ofNullable(partSiteInformationAsPlannedRepository.findByUuid(uuid))
				.orElseThrow(() -> new NoDataFoundException("No data found uuid " + uuid));
	}

}
