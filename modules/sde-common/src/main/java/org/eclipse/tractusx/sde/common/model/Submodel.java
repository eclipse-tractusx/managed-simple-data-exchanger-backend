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

package org.eclipse.tractusx.sde.common.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.tractusx.sde.common.submodel.executor.BPNDiscoveryUsecaseStep;
import org.eclipse.tractusx.sde.common.submodel.executor.DatabaseUsecaseStep;
import org.eclipse.tractusx.sde.common.submodel.executor.DigitalTwinUsecaseStep;
import org.eclipse.tractusx.sde.common.submodel.executor.EDCUsecaseStep;
import org.eclipse.tractusx.sde.common.submodel.executor.SubmodelExecutor;
import org.eclipse.tractusx.sde.common.submodel.executor.SubmoduleMapperUsecaseStep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Submodel {

	private String id;

	private String name;

	private String version;

	private String semanticId;

	private String usecases;

	private JsonObject schema;

	private Map<String, Object> properties;

	private SubmodelExecutor executor;
	
	private DigitalTwinUsecaseStep digitalTwinUseCaseStep;
	
	private BPNDiscoveryUsecaseStep bpnUseCaseTwinStep;
	
	private EDCUsecaseStep edcUseCaseStep;
	
	private DatabaseUsecaseStep databaseUseCaseStep;
	
	private SubmoduleMapperUsecaseStep submodelMapperUseCaseStep;

	public void addProperties(String key, Object value) {
		if (properties == null)
			properties = new LinkedHashMap<>();
		properties.put(key, value);
	}
}
