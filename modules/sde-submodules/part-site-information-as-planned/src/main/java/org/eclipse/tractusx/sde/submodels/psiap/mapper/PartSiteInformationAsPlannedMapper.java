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
package org.eclipse.tractusx.sde.submodels.psiap.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tractusx.sde.submodels.psiap.entity.PartSiteInformationAsPlannedEntity;
import org.eclipse.tractusx.sde.submodels.psiap.model.PartSiteInformationAsPlanned;
import org.eclipse.tractusx.sde.submodels.psiap.model.PartSiteInformationAsPlannedAspectResponse;
import org.eclipse.tractusx.sde.submodels.psiap.model.Sites;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Mapper(componentModel = "spring")
public abstract class PartSiteInformationAsPlannedMapper {

	ObjectMapper mapper=new ObjectMapper();
	
	@Mapping(target = "rowNumber", ignore = true)
	@Mapping(target = "subModelId", ignore = true)
	public abstract PartSiteInformationAsPlanned mapFrom( PartSiteInformationAsPlannedEntity partSiteInformationAsPlannedEntity);

	public abstract PartSiteInformationAsPlannedEntity mapFrom(PartSiteInformationAsPlanned partSiteInformationAsPlanned);

	@SneakyThrows
	public PartSiteInformationAsPlanned mapFrom(ObjectNode partSiteInformationAsPlanned) {
		return mapper.readValue(partSiteInformationAsPlanned.toString(), PartSiteInformationAsPlanned.class);
	}
	
	public PartSiteInformationAsPlannedEntity mapforEntity(JsonObject partSiteInformationAsPlannedAspect) {
		return new Gson().fromJson(partSiteInformationAsPlannedAspect, PartSiteInformationAsPlannedEntity.class);
	}

	public JsonObject mapFromEntity(PartSiteInformationAsPlannedEntity partSiteInformationAsPlannedAspect) {
		return new Gson().toJsonTree(partSiteInformationAsPlannedAspect).getAsJsonObject();
	}

	public JsonObject mapToResponse(String catenaXId, List<PartSiteInformationAsPlannedEntity> partSiteInformationAsPlannedEntity) {

		if (partSiteInformationAsPlannedEntity == null || partSiteInformationAsPlannedEntity.isEmpty()) {
			return null;
		}
		
		Set<Sites> sites = partSiteInformationAsPlannedEntity.stream().map(this::toSites).collect(Collectors.toSet());
		
		return new Gson().toJsonTree(PartSiteInformationAsPlannedAspectResponse.builder()
				.catenaXId(catenaXId)
				.sites(sites)
				.build()).getAsJsonObject();
	}
	
	private Sites toSites(PartSiteInformationAsPlannedEntity entity) {

		return Sites.builder()
				.catenaXSiteId(entity.getCatenaXSiteId())
				.functionValidUntil(entity.getFunctionValidUntil())
				.function(entity.getFunction())
				.functionValidFrom(entity.getFunctionValidFrom())
				.build();
	}
}
