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

package org.eclipse.tractusx.sde.common.mapper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.tractusx.sde.common.model.Submodel;
import org.mapstruct.Mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Mapper(componentModel = "spring")
public abstract class SubmodelMapper {

	Gson gson = new Gson();

	@SneakyThrows
	public JsonObject jsonfileToJsonPojo(InputStream input) {
		Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
		return gson.fromJson(reader, JsonObject.class);
	}

	@SneakyThrows
	public Submodel jsonPojoToSubmodelPojo(JsonObject input) {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(input.toString(), Submodel.class);
	}
	
	@SuppressWarnings("unchecked")
	@SneakyThrows
	public Map<Object,Object> jsonPojoToMap(JsonObject input) {
		return gson.fromJson(input, LinkedHashMap.class);
	}

}
