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

package org.eclipse.tractusx.sde.submodels.batch.mapper;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.tractusx.sde.common.mapper.AspectResponseFactory;
import org.eclipse.tractusx.sde.common.model.LocalIdentifier;
import org.eclipse.tractusx.sde.common.model.ManufacturingInformation;
import org.eclipse.tractusx.sde.common.model.PartTypeInformation;
import org.eclipse.tractusx.sde.common.model.SubmodelResultResponse;
import org.eclipse.tractusx.sde.submodels.batch.constants.BatchConstants;
import org.eclipse.tractusx.sde.submodels.batch.entity.BatchEntity;
import org.eclipse.tractusx.sde.submodels.batch.model.Batch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Mapper(componentModel = "spring")
public abstract class BatchMapper {
	
	
	ObjectMapper mapper = new ObjectMapper();
	
	@Autowired
	private AspectResponseFactory aspectResponseFactory;

	@Mapping(target = "rowNumber", ignore = true)
	public abstract Batch mapFrom(BatchEntity batch);

	public abstract BatchEntity mapFrom(Batch batch);

	@SneakyThrows
	public Batch mapFrom(ObjectNode batch) {
		return mapper.readValue(batch.toString(), Batch.class);
	}

	public BatchEntity mapforEntity(JsonObject batch) {
		return new Gson().fromJson(batch, BatchEntity.class);
	}

	public JsonObject mapFromEntity(BatchEntity batch) {
		return new Gson().toJsonTree(batch).getAsJsonObject();
	}

	public JsonObject mapToResponse(BatchEntity entity) {

		if (entity == null) {
			return null;
		}

		Batch csvObj = mapFrom(entity);

		Set<LocalIdentifier> localIdentifiers = new HashSet<>();
		localIdentifiers.add(new LocalIdentifier(BatchConstants.BATCH_ID, entity.getBatchId()));

		ManufacturingInformation manufacturingInformation = ManufacturingInformation.builder()
				.date(entity.getManufacturingDate()).country(entity.getManufacturingCountry()).build();

		PartTypeInformation partTypeInformation = PartTypeInformation.builder()
				.manufacturerPartId(entity.getManufacturerPartId()).classification(entity.getClassification())
				.nameAtManufacturer(entity.getNameAtManufacturer()).build();
		SubmodelResultResponse build = SubmodelResultResponse.builder().localIdentifiers(localIdentifiers)
				.manufacturingInformation(manufacturingInformation).partTypeInformation(partTypeInformation)
				.catenaXId(entity.getUuid()).build();

		return aspectResponseFactory.maptoReponse(csvObj, build);
	}
}
