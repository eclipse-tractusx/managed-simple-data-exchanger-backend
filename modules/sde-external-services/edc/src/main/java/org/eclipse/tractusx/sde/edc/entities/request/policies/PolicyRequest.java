/********************************************************************************
 * Copyright (c) 2022 BMW GmbH
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

package org.eclipse.tractusx.sde.edc.entities.request.policies;

import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.edc.model.policies.Obligation;
import org.eclipse.tractusx.sde.edc.model.policies.Prohibition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolicyRequest {

	@JsonProperty("@type")
	@Builder.Default
	private String type = "odrl:Set";
	
	@JsonProperty("@context")
	private Object context;
	
	@JsonProperty("@id")
	private String id;
	
	@JsonProperty("odrl:permission")
	private Object permissions;

	@JsonProperty("odrl:prohibition")
	private List<Prohibition> prohibitions;

	@JsonProperty("odrl:obligation")
	private List<Obligation> obligations;

	@JsonProperty("odrl:profile")
	private String profile;

	@JsonProperty("odrl:target")
	private Map<String, String> target;
	
	@JsonProperty("odrl:assigner")
	private Map<String, String> assigner;
	

	@SneakyThrows
	public String toJsonString() {
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
	}
}