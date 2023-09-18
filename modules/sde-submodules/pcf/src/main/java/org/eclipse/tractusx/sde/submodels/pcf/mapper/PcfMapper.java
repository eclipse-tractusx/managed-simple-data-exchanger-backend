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
package org.eclipse.tractusx.sde.submodels.pcf.mapper;

import org.eclipse.tractusx.sde.submodels.pcf.entity.PcfEntity;
import org.eclipse.tractusx.sde.submodels.pcf.model.PcfAspect;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Mapper(componentModel = "spring")
public abstract class PcfMapper {
	
	@Value(value = "${manufacturerId}")
	private String manufacturerId;

	ObjectMapper mapper = new ObjectMapper();

	@Mapping(target = "rowNumberforPcf", ignore = true)
	@Mapping(target = "subModelIdforPcf", ignore = true)
	public abstract PcfAspect mapFrom(PcfEntity aspect);

	public abstract PcfEntity mapFrom(PcfAspect aspect);

	@SneakyThrows
	public PcfAspect mapFrom(ObjectNode aspect) {
		return mapper.readValue(aspect.toString(), PcfAspect.class);
	}

	public PcfEntity mapforEntity(JsonObject aspect) {
		return new Gson().fromJson(aspect, PcfEntity.class);
	}

	public JsonObject mapFromEntity(PcfEntity aspect) {
		return new Gson().toJsonTree(aspect).getAsJsonObject();
	}

	public JsonObject mapToResponse(PcfEntity entity) {

		if (entity == null) {
			return null;
		}

		return null;

	}

}
