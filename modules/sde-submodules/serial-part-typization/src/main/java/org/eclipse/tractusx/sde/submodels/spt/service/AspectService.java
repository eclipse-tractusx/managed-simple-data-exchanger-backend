/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
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
package org.eclipse.tractusx.sde.submodels.spt.service;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DeleteDigitalTwinsFacilitator;
import org.eclipse.tractusx.sde.edc.facilitator.DeleteEDCFacilitator;
import org.eclipse.tractusx.sde.submodels.spt.entity.AspectEntity;
import org.eclipse.tractusx.sde.submodels.spt.mapper.AspectMapper;
import org.eclipse.tractusx.sde.submodels.spt.repository.AspectRepository;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AspectService {

	private final AspectRepository aspectRepository;

	private final AspectMapper aspectMapper;

	public static final String DELETED_Y = "Y";

	private final DeleteEDCFacilitator deleteEDCFacilitator;

	private final DeleteDigitalTwinsFacilitator deleteDigitalTwinsFacilitator;

	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {

		return Optional
				.ofNullable(Optional.ofNullable(aspectRepository.findByProcessId(refProcessId))
						.filter(a -> !a.isEmpty())
						.orElseThrow(() -> new NoDataFoundException(
								String.format("No data found for processid %s ", refProcessId)))
						.stream().filter(e -> !DELETED_Y.equals(e.getDeleted())).map(aspectMapper::mapFromEntity)
						.toList())
				.filter(a -> !a.isEmpty()).orElseThrow(
						() -> new NoDataFoundException("No data founds for deletion, All records are already deleted"));

	}

	public void deleteAllDataBySequence(JsonObject jsonObject) {

		AspectEntity aspectEntity = aspectMapper.mapforEntity(jsonObject);

		deleteEDCAsset(aspectEntity);

		deleteDigitalTwinsFacilitator.deleteDigitalTwinsById(aspectEntity.getShellId(), aspectEntity.getSubModelId());

		saveAspectWithDeleted(aspectEntity);
	}

	public void deleteEDCAsset(AspectEntity aspectEntity) {

		deleteEDCFacilitator.deleteContractDefination(aspectEntity.getContractDefinationId());

		deleteEDCFacilitator.deleteAccessPolicy(aspectEntity.getAccessPolicyId());

		deleteEDCFacilitator.deleteUsagePolicy(aspectEntity.getUsagePolicyId());

		deleteEDCFacilitator.deleteAssets(aspectEntity.getAssetId());
	}

	private void saveAspectWithDeleted(AspectEntity aspectEntity) {
		aspectEntity.setDeleted(DELETED_Y);
		aspectRepository.save(aspectEntity);
	}

	public JsonObject readCreatedTwinsDetails(String uuid) {
		return aspectMapper.mapToResponse(readEntity(uuid));
	}

	public AspectEntity readEntity(String uuid) {
		return Optional.ofNullable(aspectRepository.findByUuid(uuid))
				.orElseThrow(() -> new NoDataFoundException("No data found uuid " + uuid));
	}

	public int getUpdatedData(String refProcessId) {

		return (int) aspectRepository.countByUpdatedAndProcessId(CommonConstants.UPDATED_Y, refProcessId);
	}

}
