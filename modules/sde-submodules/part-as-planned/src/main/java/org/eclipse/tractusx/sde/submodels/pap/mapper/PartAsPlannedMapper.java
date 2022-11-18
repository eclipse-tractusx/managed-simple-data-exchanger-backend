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
package org.eclipse.tractusx.sde.submodels.pap.mapper;

import org.eclipse.tractusx.sde.submodels.pap.entity.PartAsPlannedEntity;
import org.eclipse.tractusx.sde.submodels.pap.model.PartAsPlanned;
import org.eclipse.tractusx.sde.submodels.pap.model.PartAsPlannedAspectResponse;
import org.eclipse.tractusx.sde.submodels.pap.model.PartTypeInformation;
import org.eclipse.tractusx.sde.submodels.pap.model.ValidityPeriod;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Mapper(componentModel = "spring")
public abstract class PartAsPlannedMapper {

	ObjectMapper mapper=new ObjectMapper();
	
	@Mapping(target = "rowNumber", ignore = true)
	@Mapping(target = "subModelId", ignore = true)
	public abstract PartAsPlanned mapFrom( PartAsPlannedEntity partAsPlannedEntity);

	public abstract PartAsPlannedEntity mapFrom(PartAsPlanned partAsPlanned);

	@SneakyThrows
	public PartAsPlanned mapFrom(ObjectNode partAsPlanned) {
		return mapper.readValue(partAsPlanned.toString(), PartAsPlanned.class);
	}
	
	public PartAsPlannedEntity mapforEntity(JsonObject partAsPlannedAspect) {
		return new Gson().fromJson(partAsPlannedAspect, PartAsPlannedEntity.class);
	}

	public JsonObject mapFromEntity(PartAsPlannedEntity partAsPlannedAspectAspect) {
		return new Gson().toJsonTree(partAsPlannedAspectAspect).getAsJsonObject();
	}

	public JsonObject mapToResponse(PartAsPlannedEntity entity) {

		if (entity == null) {
			return null;
		}

		PartTypeInformation partTypeInformation = PartTypeInformation.builder()
				.manufacturerPartId(entity.getManufacturerPartId())
				.classification(entity.getClassification())
				.nameAtManufacturer(entity.getNameAtManufacturer())
				.build();
		
		ValidityPeriod validityPeriod = ValidityPeriod.builder()
				.validFrom(entity.getValidFrom())
				.validTo(entity.getValidTo())
				.build();

		return new Gson().toJsonTree(PartAsPlannedAspectResponse.builder()
				.partTypeInformation(partTypeInformation)
				.validityPeriod(validityPeriod)
				.catenaXId(entity.getUuid())
				.build()).getAsJsonObject();
	}
}
