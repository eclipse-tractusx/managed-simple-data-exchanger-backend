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

package org.eclipse.tractusx.sde.common.submodel.executor;

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.entities.csv.RowData;
import org.eclipse.tractusx.sde.common.model.Submodel;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.Getter;

public abstract class SubmodelExecutor {

	protected Submodel submodel;

	@Getter
	JsonObject submodelSchema;

	public void init(Submodel submodel) {
		this.submodel = submodel;
		this.submodelSchema = submodel.getSchema();
	}

	public String getNameOfModel() {
		return this.submodelSchema.get("id").getAsString();
	}

	public JsonObject getSubmodelItems() {
		return submodelSchema.get("items").getAsJsonObject();
	}

	public abstract void executeCsvRecord(RowData rowData, ObjectNode jsonObject, String processId, PolicyModel policy);

	public abstract void executeJsonRecord(Integer rowIndex, ObjectNode jsonObject, String processId,
			PolicyModel policy);

	public abstract List<JsonObject> readCreatedTwinsforDelete(String refProcessId);

	public abstract List<JsonObject> readCreatedTwinsByProcessId(String refProcessId);

	public abstract void executeDeleteRecord(Integer rowIndex, JsonObject jsonObject, String delProcessId,
			String refProcessId);

	public abstract JsonObject readCreatedTwinsDetails(String uuid);

	public abstract int getUpdatedRecordCount(String processId);

}
