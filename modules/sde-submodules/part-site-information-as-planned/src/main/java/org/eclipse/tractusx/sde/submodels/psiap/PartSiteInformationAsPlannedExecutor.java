/********************************************************************************
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.sde.submodels.psiap;

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.csv.RowData;
import org.eclipse.tractusx.sde.common.submodel.executor.SubmodelExecutor;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PartSiteInformationAsPlannedExecutor extends SubmodelExecutor {@Override
	
	
	public void executeCsvRecord(RowData rowData, ObjectNode jsonObject, String processId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executeJsonRecord(Integer rowIndex, ObjectNode jsonObject, String processId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void executeDeleteRecord(JsonObject jsonObject, String delProcessId, String refProcessId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JsonObject readCreatedTwinsDetails(String uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getUpdatedRecordCount(String processId) {
		// TODO Auto-generated method stub
		return 0;
	}

}
