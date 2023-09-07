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
package org.eclipse.tractusx.sde.submodels.sluab.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tractusx.sde.common.mapper.AspectResponseFactory;
import org.eclipse.tractusx.sde.submodels.sluab.entity.SingleLevelUsageAsBuiltEntity;
import org.eclipse.tractusx.sde.submodels.sluab.model.ParentParts;
import org.eclipse.tractusx.sde.submodels.sluab.model.Quantity;
import org.eclipse.tractusx.sde.submodels.sluab.model.SingleLevelUsageAsBuilt;
import org.eclipse.tractusx.sde.submodels.sluab.model.SingleLevelUsageAsBuiltResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Mapper(componentModel = "spring")
public abstract class SingleLevelUsageAsBuiltMapper {

	@Autowired
	private AspectResponseFactory aspectResponseFactory;
	
	ObjectMapper mapper = new ObjectMapper();

	@Mapping(source = "parentUuid", target = "parentCatenaXId")
	@Mapping(source = "childUuid", target = "childCatenaXId")
	public abstract SingleLevelUsageAsBuiltEntity mapFrom(SingleLevelUsageAsBuilt singleLevelUsageAsBuilt);
	
	@Mapping(source = "parentCatenaXId", target = "parentUuid")
	@Mapping(source = "childCatenaXId", target = "childUuid")
	public abstract SingleLevelUsageAsBuilt mapFromEntityToObject(SingleLevelUsageAsBuiltEntity entity);

	@SneakyThrows
	public SingleLevelUsageAsBuilt mapFrom(ObjectNode singleLevelUsageAsBuilt) {
		return mapper.readValue(singleLevelUsageAsBuilt.toString(), SingleLevelUsageAsBuilt.class);
	}

	public SingleLevelUsageAsBuiltEntity mapforEntity(JsonObject entity) {
		return new Gson().fromJson(entity, SingleLevelUsageAsBuiltEntity.class);
	}

	public JsonObject mapFromEntity(SingleLevelUsageAsBuiltEntity singleLevelUsageAsBuilt) {
		return new Gson().toJsonTree(singleLevelUsageAsBuilt).getAsJsonObject();
	}

	public JsonObject mapToResponse(String catenaXUuid, List<SingleLevelUsageAsBuiltEntity> entity) {

		if (entity == null || entity.isEmpty()) {
			return null;
		}

		Set<ParentParts> parentPartsSet = entity.stream().map(this::toParentParts).collect(Collectors.toSet());
		SingleLevelUsageAsBuiltResponse build = SingleLevelUsageAsBuiltResponse.builder().parentParts(parentPartsSet)
				.catenaXId(catenaXUuid).build();

		SingleLevelUsageAsBuiltEntity singleLevelUsageAsBuiltEntity = entity.get(0);

		SingleLevelUsageAsBuilt csvObj = mapFromEntityToObject(singleLevelUsageAsBuiltEntity);

		return aspectResponseFactory.maptoReponse(csvObj, build);

	}

	public SingleLevelUsageAsBuiltResponse mapforResponse(JsonObject entity) {
		return new Gson().fromJson(entity, SingleLevelUsageAsBuiltResponse.class);
	}

	private ParentParts toParentParts(SingleLevelUsageAsBuiltEntity entity) {
		Quantity quantity = Quantity.builder().quantityNumber(entity.getQuantityNumber())
				.measurementUnit(entity.getMeasurementUnit()).build();

		return ParentParts.builder().parentCatenaXId(entity.getParentCatenaXId()).quantity(quantity)
				.createdOn(entity.getCreatedOn()).lastModifiedOn(entity.getLastModifiedOn()).build();
	}

}
