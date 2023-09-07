/********************************************************************************
 * Copyright (c) 2023 T-Systems International GmbH
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.sde.submodels.pcf.service;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsFacilitator;
import org.eclipse.tractusx.sde.edc.facilitator.DeleteEDCFacilitator;
import org.eclipse.tractusx.sde.submodels.pcf.entity.PcfEntity;
import org.eclipse.tractusx.sde.submodels.pcf.mapper.PcfMapper;
import org.eclipse.tractusx.sde.submodels.pcf.repository.PcfRepository;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PcfService {

	private final PcfRepository pcfRepository;

	private final PcfMapper pcfMapper;

	public static final String DELETED_Y = "Y";

	private final DeleteEDCFacilitator deleteEDCFacilitator;

	private final DigitalTwinsFacilitator deleteDigitalTwinsFacilitator;

	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {

		return Optional
				.ofNullable(Optional.ofNullable(pcfRepository.findByProcessIdforPcf(refProcessId)).filter(a -> !a.isEmpty())
						.orElseThrow(() -> new NoDataFoundException(
								String.format("No data found for processid %s ", refProcessId)))
						.stream().filter(e -> !DELETED_Y.equals(e.getDeletedforPcf())).map(pcfMapper::mapFromEntity)
						.toList())
				.filter(a -> !a.isEmpty()).orElseThrow(
						() -> new NoDataFoundException("No data founds for deletion, All records are already deleted"));

	}

	public void deleteAllDataBySequence(JsonObject jsonObject) {

		PcfEntity pcfEntity = pcfMapper.mapforEntity(jsonObject);

		deleteEDCAsset(pcfEntity);

		deleteDigitalTwinsFacilitator.deleteSubmodelfromShellById(pcfEntity.getShellIdforPcf(),
				pcfEntity.getSubModelIdforPcf());

		saveAspectWithDeleted(pcfEntity);
	}

	public void deleteEDCAsset(PcfEntity pcfEntity) {

		deleteEDCFacilitator.deleteContractDefination(pcfEntity.getContractDefinationIdforPcf());

		deleteEDCFacilitator.deleteAccessPolicy(pcfEntity.getAccessPolicyIdforPcf());

		deleteEDCFacilitator.deleteUsagePolicy(pcfEntity.getUsagePolicyIdforPcf());

		deleteEDCFacilitator.deleteAssets(pcfEntity.getAssetIdforPcf());
	}

	private void saveAspectWithDeleted(PcfEntity pcfEntity) {
		pcfEntity.setDeletedforPcf(DELETED_Y);
		pcfRepository.save(pcfEntity);
	}

	public JsonObject readCreatedTwinsDetails(String uuid) {
		return pcfMapper.mapToResponse(readEntity(uuid));
	}

	public PcfEntity readEntity(String id) {
		Optional<PcfEntity> findById = pcfRepository.findById(id);
		if (!findById.isPresent())
			throw new NoDataFoundException("No data found uuid " + id);
		return findById.get();
	}

	public int getUpdatedData(String refProcessId) {

		return (int) pcfRepository.countByUpdatedAndProcessId(CommonConstants.UPDATED_Y, refProcessId);
	}

}
