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

package org.eclipse.tractusx.sde.common.extensions;

import java.io.InputStream;

import org.eclipse.tractusx.sde.common.mapper.SubmodelMapper;
import org.eclipse.tractusx.sde.common.model.Submodel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Component
public abstract class SubmodelExtension {

	@Autowired
	private SubmodelMapper submodelMapper;

	@SneakyThrows
	public Submodel loadSubmodel(InputStream input) {
		JsonObject schema = submodelMapper.jsonfileToJsonPojo(input);
		
		return Submodel.builder()
				.id(schema.get("id").getAsString())
				.name(schema.get("title").getAsString())
				.version(schema.get("version").getAsString())
				.semanticId(schema.get("semantic_id").getAsString())
				.schema(schema).build();
	}

	public abstract Submodel submodel();

}
