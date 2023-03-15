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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

public abstract class UsecaseExtension {

	private JsonObject json;

	@SneakyThrows
	public JsonObject loadUsecae(String useCase) {
		json.addProperty("id", useCase);
		return json;
	}

	public void addSubmodel(String submodelName) {
		JsonArray asJsonObject = json.get("submdel").getAsJsonArray();
		if (asJsonObject == null)
			asJsonObject = new JsonArray();
		asJsonObject.add(submodelName);
	}

	public abstract JsonObject getUseCase();
}
