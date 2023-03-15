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

package org.eclipse.tractusx.sde.common.submodel.executor.create.steps.impl;

import java.util.Set;

import org.eclipse.tractusx.sde.common.exception.JsonRecordHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Component
@AllArgsConstructor
public class JsonRecordFormating extends Step {

	private final RecordProcessUtils recordProcessUtils;

	@SneakyThrows
	public ObjectNode run(Integer rowIndex, ObjectNode rowjObject, String processId) {

		JsonObject submodelProperties = getSubmodelProperties();
		Set<String> fields = submodelProperties.keySet();

		int colomnIndex = 0;
		for (String ele : fields) {
			try {
				JsonObject jObject = submodelProperties.get(ele).getAsJsonObject();

				String fieldValue = null;
				JsonNode jsonValuenode = rowjObject.get(ele);
				if (!jsonValuenode.isNull())
					fieldValue = jsonValuenode.asText();

				recordProcessUtils.setFieldValue(rowjObject, ele, jObject, fieldValue);

				colomnIndex++;

			} catch (ValidationException errorMessages) {
				throw new JsonRecordHandlerUseCaseException(rowIndex, colomnIndex,
						ele + ":" + errorMessages.toString());
			}
		}

		return rowjObject;
	}

}
