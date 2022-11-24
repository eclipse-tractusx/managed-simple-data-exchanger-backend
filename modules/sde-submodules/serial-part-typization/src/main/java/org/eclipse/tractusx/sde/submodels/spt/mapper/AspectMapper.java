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

package org.eclipse.tractusx.sde.submodels.spt.mapper;

import java.util.ArrayList;
import java.util.stream.Stream;

import org.eclipse.tractusx.sde.common.enums.OptionalIdentifierKeyEnum;
import org.eclipse.tractusx.sde.common.model.LocalIdentifier;
import org.eclipse.tractusx.sde.common.model.ManufacturingInformation;
import org.eclipse.tractusx.sde.common.model.PartTypeInformation;
import org.eclipse.tractusx.sde.common.model.SubmodelResultResponse;
import org.eclipse.tractusx.sde.submodels.spt.entity.AspectEntity;
import org.eclipse.tractusx.sde.submodels.spt.model.Aspect;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Mapper(componentModel = "spring")
public abstract class AspectMapper {
	@Value(value = "${manufacturerId}")
	private String manufacturerId;

	ObjectMapper mapper=new ObjectMapper();
	
	@Mapping(target = "rowNumber", ignore = true)
	@Mapping(target = "subModelId", ignore = true)
	public abstract Aspect mapFrom(AspectEntity aspect);

	@Mapping(source = "optionalIdentifierKey", target = "optionalIdentifierKey", qualifiedByName = "prettyName")
	public abstract AspectEntity mapFrom(Aspect aspect);

	@SneakyThrows
	public Aspect mapFrom(ObjectNode aspect) {
		return mapper.readValue(aspect.toString(), Aspect.class);
	}
	
	public AspectEntity mapforEntity(JsonObject aspect) {
		return new Gson().fromJson(aspect, AspectEntity.class);
	}

	public JsonObject mapFromEntity(AspectEntity aspect) {
		return new Gson().toJsonTree(aspect).getAsJsonObject();
	}

	public JsonObject mapToResponse(AspectEntity entity) {

		if (entity == null) {
			return null;
		}

		ArrayList<LocalIdentifier> localIdentifiers = new ArrayList<>();
		localIdentifiers.add(new LocalIdentifier("PartInstanceID", entity.getPartInstanceId()));
		localIdentifiers.add(new LocalIdentifier("ManufacturerPartID", entity.getManufacturerPartId()));
		localIdentifiers.add(new LocalIdentifier("ManufacturerID", manufacturerId));
		if (entity.getOptionalIdentifierKey() != null && entity.getOptionalIdentifierValue() != null) {
			localIdentifiers
					.add(new LocalIdentifier(entity.getOptionalIdentifierKey(), entity.getOptionalIdentifierValue()));
		}

		ManufacturingInformation manufacturingInformation = ManufacturingInformation.builder()
				.country(entity.getManufacturingCountry()).date(entity.getManufacturingDate()).build();

		PartTypeInformation partTypeInformation = PartTypeInformation.builder()
				.manufacturerPartID(entity.getManufacturerPartId()).customerPartId(entity.getCustomerPartId())
				.classification(entity.getClassification()).nameAtManufacturer(entity.getNameAtManufacturer())
				.nameAtCustomer(entity.getNameAtCustomer()).build();

		return new Gson().toJsonTree(SubmodelResultResponse.builder().localIdentifiers(localIdentifiers)
				.manufacturingInformation(manufacturingInformation).partTypeInformation(partTypeInformation)
				.catenaXId(entity.getUuid())
				.build()).getAsJsonObject();
	}

	@Named("prettyName")
	String getPrettyName(String optionalIdentifierKey) {
		return optionalIdentifierKey == null || optionalIdentifierKey.isBlank()  ? null
				: Stream.of(OptionalIdentifierKeyEnum.values())
						.filter(v -> v.getPrettyName().equalsIgnoreCase(optionalIdentifierKey)).findFirst().get()
						.getPrettyName();
	}
}
