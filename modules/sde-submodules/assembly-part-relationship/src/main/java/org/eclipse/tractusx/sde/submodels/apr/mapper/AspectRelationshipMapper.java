/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
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

package org.eclipse.tractusx.sde.submodels.apr.mapper;

import java.util.List;

import org.eclipse.tractusx.sde.submodels.apr.entity.AspectRelationshipEntity;
import org.eclipse.tractusx.sde.submodels.apr.model.AspectRelationship;
import org.eclipse.tractusx.sde.submodels.apr.model.AspectRelationshipResponse;
import org.eclipse.tractusx.sde.submodels.apr.model.ChildPart;
import org.eclipse.tractusx.sde.submodels.apr.model.MeasurementUnit;
import org.eclipse.tractusx.sde.submodels.apr.model.Quantity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

@Mapper(componentModel = "spring")
public abstract class AspectRelationshipMapper {

	@Mapping(source = "parentUuid", target = "parentCatenaXId")
	@Mapping(source = "childUuid", target = "childCatenaXId")
	public abstract AspectRelationshipEntity mapFrom(AspectRelationship aspectRelationShip);

	public AspectRelationship mapFrom(JsonObject aspectRelationship) {
		Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
				.setPrettyPrinting().create();
		return gson.fromJson(aspectRelationship, AspectRelationship.class);
	}

	public AspectRelationshipEntity mapforEntity(JsonObject batch) {
		return new Gson().fromJson(batch, AspectRelationshipEntity.class);
	}
	
	public JsonObject mapFromEntity(AspectRelationshipEntity aspectRelationship) {
		return new Gson().toJsonTree(aspectRelationship).getAsJsonObject();
	}
	public JsonObject mapToResponse(String parentCatenaXUuid,
			List<AspectRelationshipEntity> aspectRelationships) {

		if (aspectRelationships == null || aspectRelationships.isEmpty()) {
			return null;
		}

		List<ChildPart> childParts = aspectRelationships.stream().map(this::toChildPart).toList();
		return new Gson().toJsonTree(AspectRelationshipResponse.builder().catenaXId(parentCatenaXUuid).childParts(childParts).build()).getAsJsonObject();

	}

	private ChildPart toChildPart(AspectRelationshipEntity entity) {
		Quantity quantity = Quantity.builder().quantityNumber(entity.getQuantityNumber())
				.measurementUnit(new MeasurementUnit(entity.getMeasurementUnitLexicalValue(), entity.getDataTypeUri()))
				.build();

		return ChildPart.builder().lifecycleContext(entity.getLifecycleContext()).assembledOn(entity.getAssembledOn())
				.childCatenaXId(entity.getChildCatenaXId()).quantity(quantity).build();
	}

}
