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
import org.eclipse.tractusx.sde.common.submodel.executor.SubmoduleMapperUsecaseStep;
import org.eclipse.tractusx.sde.common.utils.LogUtil;
import org.eclipse.tractusx.sde.core.processreport.repository.SubmodelCustomHistoryGenerator;
import org.eclipse.tractusx.sde.core.service.SubmodelService;
import org.eclipse.tractusx.sde.core.utils.SubmoduleUtility;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("databaseUsecaseHandler")
@RequiredArgsConstructor
public class DatabaseUsecaseHandler extends Step implements DatabaseUsecaseStep {

	private final SubmodelCustomHistoryGenerator submodelCustomHistoryGenerator;

	private final SubmodelService submodelService;
	private final SubmoduleUtility submoduleUtility;
	
	@Qualifier("submoduleResponseHandler")
	private final SubmoduleMapperUsecaseStep submoduleResponseHandler;
	

	@SneakyThrows
	@Override
	public JsonNode run(Integer rowIndex, ObjectNode jsonObject, String processId, PolicyModel policy) {

		Submodel schemaObj = submodelService.findSubmodelByNameAsSubmdelObject(getNameOfModel());
		List<String> columns = submoduleUtility.getTableColomnHeader(schemaObj);
		String tableName = submoduleUtility.getTableName(schemaObj);

		submodelCustomHistoryGenerator.saveSubmodelData(columns, tableName, processId, jsonObject,
				getDatabaseIdentifierSpecsOfModel());

		return jsonObject;
	}

	@SneakyThrows
	@Override
	public void saveSubmoduleWithDeleted(Integer rowIndex, JsonObject jsonObject, String delProcessId,
			String refProcessId) {

		String identifier = extractExactFieldName(getIdentifierOfModel());
		String uuid = jsonObject.get(identifier).getAsString();
		Submodel schemaObj = submodelService.findSubmodelByNameAsSubmdelObject(getNameOfModel());
		String tableName = submoduleUtility.getTableName(schemaObj);
		submodelCustomHistoryGenerator.saveAspectWithDeleted(uuid, tableName, identifier);
	}

	@SneakyThrows
	@Override
	public List<JsonObject> readCreatedTwins(String refProcessId, String fetchNotDeletedRecord) {
		Submodel schemaObj = submodelService.findSubmodelByNameAsSubmdelObject(getNameOfModel());
		List<String> columns = submoduleUtility.getTableColomnHeader(schemaObj);
		String tableName = submoduleUtility.getTableName(schemaObj);
		
		submoduleResponseHandler.init(schemaObj.getSchema());
		
		return submodelCustomHistoryGenerator.findAllSubmoduleAsJsonList(columns,
				tableName, refProcessId, fetchNotDeletedRecord)
				.stream()
				.map(submoduleResponseHandler::mapJsonbjectToFormatedResponse).toList();
	}

	@SneakyThrows
	@Override
	public JsonObject readCreatedTwinsBySpecifyColomn(String sematicId, String value) {

		List<Submodel> allSubmodels = submodelService.getAllSubmodels();
		List<Submodel> list = allSubmodels.stream().filter(ele -> ele.getSemanticId().startsWith(sematicId)).toList();

		List<JsonObject> jsonObjectList = list.stream().flatMap(schemaObj -> {
			try {
				this.init(schemaObj.getSchema());
				submoduleResponseHandler.init(schemaObj.getSchema());

				List<String> columns = submoduleUtility.getTableColomnHeader(schemaObj);
				String tableName = submoduleUtility.getTableName(schemaObj);

				return submodelCustomHistoryGenerator
						.readCreatedTwinsDetails(columns, tableName, getIdentifierValuesAsList(value),
								getDatabaseIdentifierSpecsOfModel())
						.stream().map(submoduleResponseHandler::mapJsonbjectToFormatedResponse);
			} catch (Exception e) {
				log.debug(LogUtil.encode("Exception for " + sematicId +", " + value + ", " + e.getMessage()));
			}
			return null;
		}).filter(ele -> Optional.ofNullable(ele).isPresent()).toList();

		if (jsonObjectList.isEmpty())
			throw new NoDataFoundException("No data founds for " + sematicId + ", " + value);
		else
			return jsonObjectList.get(0);
	}

	@SneakyThrows
	@Override
	public JsonObject readCreatedTwinsDetails(String uuid) {

		Submodel schemaObj = submodelService.findSubmodelByNameAsSubmdelObject(getNameOfModel());
		List<String> columns = submoduleUtility.getTableColomnHeader(schemaObj);
		String tableName = submoduleUtility.getTableName(schemaObj);

		return submodelCustomHistoryGenerator.readCreatedTwinsDetails(columns, tableName,
				getIdentifierValuesAsList(uuid), getDatabaseIdentifierSpecsOfModel()).get(0);
	}

	@SneakyThrows
	@Override
	public int getUpdatedData(String processId) {
		Submodel schemaObj = submodelService.findSubmodelByNameAsSubmdelObject(getNameOfModel());
		String tableName = submoduleUtility.getTableName(schemaObj);
		return submodelCustomHistoryGenerator.countUpdatedRecordCount(tableName, CommonConstants.UPDATED_Y, processId);
	}

}