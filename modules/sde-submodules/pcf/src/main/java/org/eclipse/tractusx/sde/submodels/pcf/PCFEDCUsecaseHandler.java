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

package org.eclipse.tractusx.sde.submodels.pcf;

import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.submodel.executor.EDCUsecaseStep;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("pcfEDCUsecaseHandler")
@RequiredArgsConstructor
public class PCFEDCUsecaseHandler extends Step implements EDCUsecaseStep {

	@SneakyThrows
	public ObjectNode run(Integer rowNumber, ObjectNode objectNode, String processId, PolicyModel policy) {
		log.warn("No need to create EDC asset for PCF exchange");
		return objectNode;
	}

	public void delete(Integer rowIndex, JsonObject jsonObject, String delProcessId, String refProcessId) {
		log.warn("No need to delete EDC asset for PCF exchange");
	}
}