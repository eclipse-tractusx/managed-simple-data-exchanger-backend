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

package org.eclipse.tractusx.sde.common.validators;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.tractusx.sde.common.model.Submodel;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

@Service
public class SubmodelCSVValidator {

	public boolean validate(Submodel submodelSchemaObject, List<String> columns) {

		JsonObject submodelSchema = submodelSchemaObject.getSchema();
		JsonObject items = submodelSchema.get("items").getAsJsonObject();
		JsonObject asJsonObject = items.get("properties").getAsJsonObject();

		Set<String> keySet = asJsonObject.keySet();
		Set<String> targetSet = new LinkedHashSet<>(columns);

		return keySet.equals(targetSet);
		
	}
}

