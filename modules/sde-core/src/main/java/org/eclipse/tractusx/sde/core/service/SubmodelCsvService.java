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
package org.eclipse.tractusx.sde.core.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.common.model.Submodel;
import org.eclipse.tractusx.sde.core.processreport.repository.SubmodelCustomHistoryGenerator;
import org.eclipse.tractusx.sde.core.utils.SubmoduleUtility;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Service
@AllArgsConstructor
public class SubmodelCsvService {

	private final SubmodelService submodelService;

	private final SubmodelCustomHistoryGenerator submodelCustomHistoryGenerator;

	private final SubmoduleUtility submoduleUtility;

	private static final List<String> TYPES = List.of("sample", "template");

	@SneakyThrows
	public List<List<String>> findSubmodelCsv(String submodelName, String type) {

		List<List<String>> jsonObjectList = new ArrayList<>();
		if (type != null && TYPES.contains(type.toLowerCase())) {

			JsonObject schemaObject = submodelService.findSubmodelByNameAsSubmdelObject(submodelName).getSchema();
			JsonObject asJsonObject = schemaObject.get("items").getAsJsonObject().get("properties").getAsJsonObject();
			List<String> headerList = asJsonObject.keySet().stream().toList();
			jsonObjectList.add(headerList);

			if ("sample".equalsIgnoreCase(type)) {
				List<String> listexampleValue = new ArrayList<>();
				JsonObject jsonNode = schemaObject.getAsJsonArray("examples").get(0).getAsJsonObject();
				headerList.stream().forEach(key -> listexampleValue.add(jsonNode.get(key).getAsString()));
				jsonObjectList.add(listexampleValue);
			}
		} else {
			throw new ValidationException("Unknown CSV type: " + type + " for submodel: " + submodelName);
		}
		return jsonObjectList;
	}

	@SneakyThrows
	public List<List<String>> findAllSubmodelCsvHistory(String submodel, String processId) {

		List<List<String>> records = new LinkedList<>();
		Submodel schemaObj = submodelService.findSubmodelByNameAsSubmdelObject(submodel);
		List<String> headerName = submoduleUtility.getTableColomnHeaderForCSV(schemaObj);

		records.add(headerName);

		String tableName = submoduleUtility.getTableName(schemaObj);

		records.addAll(submodelCustomHistoryGenerator.findAllSubmodelCsvHistory(headerName, tableName, processId));

		return records;
	}



}
