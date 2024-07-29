/********************************************************************************
 * Copyright (c) 2022,2024 T-Systems International GmbH
 * Copyright (c) 2022,2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.core.registry;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.tractusx.sde.common.extensions.SubmodelExtension;
import org.eclipse.tractusx.sde.common.model.Submodel;
import org.eclipse.tractusx.sde.core.processreport.repository.SubmodelCustomHistoryGenerator;
import org.eclipse.tractusx.sde.core.utils.SubmoduleUtility;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SubmodelRegistration {

	private List<Submodel> submodelList;

	private final SubmodelCustomHistoryGenerator submodelCustomHistoryGenerator;
	private final SubmoduleUtility submoduleUtility;

	public SubmodelRegistration(SubmodelCustomHistoryGenerator submodelCustomHistoryGenerator,
			SubmoduleUtility submoduleUtility) {
		submodelList = new LinkedList<>();
		this.submodelCustomHistoryGenerator = submodelCustomHistoryGenerator;
		this.submoduleUtility = submoduleUtility;
	}

	@SneakyThrows
	public void register(SubmodelExtension submodelService) {
		Submodel submodel = submodelService.submodel();
		log.debug(submodel.toString());

		
		List<String> columns = submoduleUtility.getTableColomnHeader(submodel);
		String tableName = submoduleUtility.getTableName(submodel);
		JsonElement jsonElement = submodel.getSchema().get("addOn");

		if (jsonElement != null && !jsonElement.isJsonNull()) {
			String pkCol = extractExactFieldName(jsonElement.getAsJsonObject().get("identifier").getAsString());
			
			JsonElement databaseIdentifierSpecs = jsonElement.getAsJsonObject().get("databaseIdentifierSpecs");
			
			List<String> databaseIdentifierCols= null;
			
			if (databaseIdentifierSpecs != null && !databaseIdentifierSpecs.isJsonNull()) {
				databaseIdentifierCols = databaseIdentifierSpecs.getAsJsonArray().asList().stream()
						.map(ele -> extractExactFieldName(ele.getAsString())).toList();
			}
			
			submodelCustomHistoryGenerator.checkTableIfNotExistCreate(submodel.getSchema(), columns, tableName, pkCol, databaseIdentifierCols);
		}
		log.info(submodel.getSchema().get("id") + " sub model registered successfully");
		submodelList.add(submodel);
	}

	public List<Submodel> getModels() {
		return this.submodelList;
	}
	
	private String extractExactFieldName(String str) {

		if (str.startsWith("${")) {
			return str.replace("${", "").replace("}", "").trim();
		} else {
			return str;
		}
	}

}