/********************************************************************************
 * Copyright (c) 2022, 2024 T-Systems International GmbH
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.common.mapper;

import java.util.Map;

import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.mapstruct.Mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Mapper(componentModel = "spring")
public abstract class JsonObjectMapper {

	ObjectMapper mapper = new ObjectMapper();
	Gson gson=new Gson();
	
	@SneakyThrows
	public JsonNode gsonObjectToJsonNode(JsonObject json) {
		String jsonStr = "{}";
		if(json != null) {
			jsonStr = gson.toJson(json);
		}
		return mapper.readTree(jsonStr);
	}
	
	@SneakyThrows
	public ObjectNode submodelFileRequestToJsonNodePojo(PolicyModel submodelFileRequest) {
		mapper.findAndRegisterModules();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		return mapper.convertValue(submodelFileRequest, ObjectNode.class);
	}

	@SuppressWarnings("deprecation")
	@SneakyThrows
	public ObjectNode submodelJsonRequestToJsonPojo(ObjectNode jobj, Map<String, Object> mps) {
		jobj.putAll(mapper.convertValue(mps, ObjectNode.class));
		return jobj;
	}
	
	@SneakyThrows
	public JsonNode objectToJsonNode(Object jobj) {
		return mapper.convertValue(jobj, JsonNode.class);
	}
}
