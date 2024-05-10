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

package org.eclipse.tractusx.sde.core.utils;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.tractusx.sde.common.constants.SubmoduleCommonColumnsConstant;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.model.Submodel;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Component
public class SubmoduleUtility {

	public String getTableName(Submodel schemaObj) throws ServiceException {
		String tableName = schemaObj.getProperties().get("tableName").toString();
		if (tableName == null)
			throw new ServiceException("The submodel table name not found for processing");
		return tableName;
	}


	public List<String> getTableColomnHeader(Submodel schemaObj) {
		List<String> tableColomnHeader = getTableColomnHeaderForCSV(schemaObj);
		tableColomnHeader.add(SubmoduleCommonColumnsConstant.PROCESS_ID);
		tableColomnHeader.add(SubmoduleCommonColumnsConstant.DELETED);
		tableColomnHeader.add(SubmoduleCommonColumnsConstant.UPDATED);
		tableColomnHeader.add(SubmoduleCommonColumnsConstant.SHELL_ACCESS_RULE_IDS);
		return tableColomnHeader;
	}
	
	public List<String> getTableColomnHeaderForCSV(Submodel schemaObj) {
		List<String> headerName = getCSVHeader(schemaObj);
		headerName.add(SubmoduleCommonColumnsConstant.SHELL_ID);
		headerName.add(SubmoduleCommonColumnsConstant.SUBMODULE_ID);
		headerName.add(SubmoduleCommonColumnsConstant.ASSET_ID);
		headerName.add(SubmoduleCommonColumnsConstant.ACCESS_POLICY_ID);
		headerName.add(SubmoduleCommonColumnsConstant.USAGE_POLICY_ID);
		headerName.add(SubmoduleCommonColumnsConstant.CONTRACT_DEFINATION_ID);
		return headerName;
	}

	public List<String> getCSVHeader(Submodel schemaObj) {
		
		JsonObject schemaObject = schemaObj.getSchema();
		
		JsonObject asJsonObject = schemaObject.get("items").getAsJsonObject().get("properties").getAsJsonObject();
		List<String> headerList = asJsonObject.keySet().stream().toList();
		List<String> headerName = new LinkedList<>();
		headerName.addAll(headerList);

		JsonElement autoPopulatedfields = schemaObject.get("addOn").getAsJsonObject().get("autoPopulatedfields");
		if (autoPopulatedfields != null && autoPopulatedfields.isJsonArray()) {
			List<String> autoPopulatefield = autoPopulatedfields.getAsJsonArray().asList().stream()
					.map(ele -> extractExactFieldName(ele.getAsJsonObject().get("key").getAsString())).toList();
			headerName.addAll(autoPopulatefield);
		}
		
		return headerName;
	}

	private String extractExactFieldName(String str) {

		if (str.startsWith("${")) {
			return str.replace("${", "").replace("}", "").trim();
		} else {
			return str;
		}
	}
}
