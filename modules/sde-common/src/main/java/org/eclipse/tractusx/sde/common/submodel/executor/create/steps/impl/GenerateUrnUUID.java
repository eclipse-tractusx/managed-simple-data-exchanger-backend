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

package org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl;

import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.common.utils.UUIdGenerator;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.SneakyThrows;

@Component
public class GenerateUrnUUID extends Step {

	@SneakyThrows
	public ObjectNode run(ObjectNode jsonObject, String processId) {
		
		JsonNode jsonNode = jsonObject.get("uuid");
		if (jsonNode == null || jsonNode.isNull())
			return jsonObject;
		
		String uUID = jsonNode.asText();
		if (uUID == null || uUID.isBlank() || uUID.equals("null")) {
			jsonObject.put("uuid", UUIdGenerator.getUrnUuid());
		} else if (!uUID.startsWith(UUIdGenerator.URN_UUID_PREFIX)) {
			String concat = UUIdGenerator.URN_UUID_PREFIX.concat(uUID);
			jsonObject.put("uuid", concat);
		}
		return jsonObject;
	}

}
