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

package org.eclipse.tractusx.sde.digitaltwins.entities.response;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.KeyValuePair;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.MultiLanguage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShellDescriptorResponse {
	private String idShort;
	
	@JsonProperty("identification")
	private String identification;
	
	@JsonProperty("id")
	private String id;
	
	private List<MultiLanguage> description;
	
	private String globalAssetId;
	
	private List<KeyValuePair> specificAssetIds;
	
	private List<SubModelResponse> submodelDescriptors;
	
	public String getIdentification() {
		if (StringUtils.isBlank(this.identification)) {
			identification = this.id;
		}
		return this.identification;
	}
}
