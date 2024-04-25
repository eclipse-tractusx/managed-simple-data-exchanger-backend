/********************************************************************************
 * Copyright (c) 2024 T-Systems International GmbH
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.core.submodel.executor.step;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.common.model.Submodel;
import org.eclipse.tractusx.sde.common.submodel.executor.DatabaseUsecaseStep;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.core.processreport.repository.SubmodelCustomHistoryGenerator;
import org.eclipse.tractusx.sde.core.service.SubmodelService;
import org.eclipse.tractusx.sde.core.utils.SubmoduleUtility;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("DatabaseUsecaseHandler")
@RequiredArgsConstructor
public class DatabaseUsecaseHandler extends Step implements DatabaseUsecaseStep {

	private final SubmodelCustomHistoryGenerator submodelCustomHistoryGenerator;

	private final SubmodelService submodelService;
	private final SubmoduleUtility submoduleUtility;

	@SneakyThrows
	public JsonNode run(Integer rowIndex, ObjectNode jsonObject, String processId, PolicyModel policy) {

		Submodel schemaObj = submodelService.findSubmodelByNameAsSubmdelObject(getNameOfModel());
		List<String> columns = submoduleUtility.getTableColomnHeader(schemaObj);
		String tableName = submoduleUtility.getTableName(schemaObj);

		submodelCustomHistoryGenerator.saveSubmodelData(columns, tableName, processId, jsonObject,
				extractExactFieldName(getIdentifierOfModel()));

		return jsonObject;
	}

	@SneakyThrows
	@Override
	public void saveSubmoduleWithDeleted(Integer rowIndex, JsonObject jsonObject, String delProcessId,
			String refProcessId) {

		String uuid = jsonObject.get(extractExactFieldName(getIdentifierOfModel())).getAsString();
		Submodel schemaObj = submodelService.findSubmodelByNameAsSubmdelObject(getNameOfModel());
		String tableName = submoduleUtility.getTableName(schemaObj);

		submodelCustomHistoryGenerator.saveAspectWithDeleted(uuid, tableName,
				extractExactFieldName(getIdentifierOfModel()));
	}

	@SneakyThrows
	public List<JsonObject> readCreatedTwins(String refProcessId, String fetchNotDeletedRecord) {
		Submodel schemaObj = submodelService.findSubmodelByNameAsSubmdelObject(getNameOfModel());
		List<String> columns = submoduleUtility.getTableColomnHeader(schemaObj);
		String tableName = submoduleUtility.getTableName(schemaObj);

		List<JsonObject> allSubmoduleAsJsonList = submodelCustomHistoryGenerator.findAllSubmoduleAsJsonList(columns,
				tableName, refProcessId, fetchNotDeletedRecord);

		if (allSubmoduleAsJsonList.isEmpty())
			throw new NoDataFoundException("No data founds for deletion " + refProcessId);

		return allSubmoduleAsJsonList;
	}

	@SneakyThrows
	public JsonObject readCreatedTwinsBySpecifyColomn(String sematicId, String basedCol, String value) {

		List<Submodel> allSubmodels = submodelService.getAllSubmodels();
		List<Submodel> list = allSubmodels.stream().filter(ele -> ele.getSemanticId().startsWith(sematicId)).toList();

		List<JsonObject> jsonObjectList = list.stream().flatMap(schemaObj -> {
			try {
				List<String> columns = submoduleUtility.getTableColomnHeader(schemaObj);
				String tableName = submoduleUtility.getTableName(schemaObj);
				return submodelCustomHistoryGenerator.readCreatedTwinsDetails(columns, tableName, value, basedCol).stream();
			} catch (Exception e) {
				log.debug("Exception for {}, {}, {}, {}", sematicId, basedCol, value, e.getMessage());
			}
			return null;
		})
				.filter(ele-> Optional.ofNullable(ele).isPresent())
				.toList();
		
		if (jsonObjectList.isEmpty())
			throw new NoDataFoundException("No data founds for "+sematicId +", " + value);
		else
			return jsonObjectList.get(0);
	}

	@SneakyThrows
	public JsonObject readCreatedTwinsDetails(String uuid) {
		Submodel schemaObj = submodelService.findSubmodelByNameAsSubmdelObject(getNameOfModel());
		List<String> columns = submoduleUtility.getTableColomnHeader(schemaObj);
		String tableName = submoduleUtility.getTableName(schemaObj);
		return submodelCustomHistoryGenerator.readCreatedTwinsDetails(columns, tableName, uuid,
				extractExactFieldName(getIdentifierOfModel())).get(0);
	}

	@SneakyThrows
	public int getUpdatedData(String processId) {
		Submodel schemaObj = submodelService.findSubmodelByNameAsSubmdelObject(getNameOfModel());
		String tableName = submoduleUtility.getTableName(schemaObj);
		return submodelCustomHistoryGenerator.countUpdatedRecordCount(tableName, CommonConstants.UPDATED_Y, processId);
	}

}
