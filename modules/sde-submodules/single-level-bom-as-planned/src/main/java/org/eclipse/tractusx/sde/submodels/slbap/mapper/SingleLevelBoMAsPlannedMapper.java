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
package org.eclipse.tractusx.sde.submodels.slbap.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tractusx.sde.submodels.slbap.entity.SingleLevelBoMAsPlannedEntity;
import org.eclipse.tractusx.sde.submodels.slbap.model.ChildParts;
import org.eclipse.tractusx.sde.submodels.slbap.model.MeasurementUnit;
import org.eclipse.tractusx.sde.submodels.slbap.model.Quantity;
import org.eclipse.tractusx.sde.submodels.slbap.model.SingleLevelBoMAsPlanned;
import org.eclipse.tractusx.sde.submodels.slbap.model.SingleLevelBoMAsPlannedAspectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Mapper(componentModel = "spring")
public abstract class SingleLevelBoMAsPlannedMapper {
	
	ObjectMapper mapper=new ObjectMapper();

	@Mapping(source = "parentUuid", target = "parentCatenaXId")
	@Mapping(source = "childUuid", target = "childCatenaXId")
	public abstract SingleLevelBoMAsPlannedEntity mapFrom(SingleLevelBoMAsPlanned singleLevelBoMAsPlanned);

	@SneakyThrows
	public SingleLevelBoMAsPlanned mapFrom(ObjectNode singleLevelBoMAsPlanned) {
		return mapper.readValue(singleLevelBoMAsPlanned.toString(), SingleLevelBoMAsPlanned.class);
	}
	
	public SingleLevelBoMAsPlannedEntity mapforEntity(JsonObject singleLevelBoMAsPlanned) {
		return new Gson().fromJson(singleLevelBoMAsPlanned, SingleLevelBoMAsPlannedEntity.class);
	}

	public JsonObject mapFromEntity(SingleLevelBoMAsPlannedEntity singleLevelBoMAsPlannedEntity) {
		return new Gson().toJsonTree(singleLevelBoMAsPlannedEntity).getAsJsonObject();
	}

	public JsonObject mapToResponse(String parentCatenaXUuid, List<SingleLevelBoMAsPlannedEntity> singleLevelBoMAsPlannedEntity) {

		if (singleLevelBoMAsPlannedEntity == null || singleLevelBoMAsPlannedEntity.isEmpty()) {
			return null;
		}
		
		Set<ChildParts> childPartsSet = singleLevelBoMAsPlannedEntity.stream().map(this::toChildPart).collect(Collectors.toSet());
		
		return new Gson().toJsonTree(SingleLevelBoMAsPlannedAspectResponse.builder()
				.catenaXId(parentCatenaXUuid)
				.childParts(childPartsSet)
				.build()).getAsJsonObject();
	}
	
	private ChildParts toChildPart(SingleLevelBoMAsPlannedEntity entity) {
		
		Quantity quantity = Quantity.builder().quantityNumber(entity.getQuantityNumber())
							.measurementUnit(MeasurementUnit.builder()
									.lexicalValue(entity.getMeasurementUnitLexicalValue())
									.datatypeURI(entity.getDatatypeURI())
									.build())
							.build();

		return ChildParts.builder()
				.quantity(quantity)
				.createdOn(entity.getCreatedOn())
				.lastModifiedOn(entity.getLastModifiedOn())
				.childCatenaXId(entity.getChildCatenaXId())
				.build();
	}

}
