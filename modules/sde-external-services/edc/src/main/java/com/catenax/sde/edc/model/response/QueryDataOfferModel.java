/********************************************************************************
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the CatenaX (ng) GitHub Organisation
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

package com.catenax.sde.edc.model.response;

import com.catenax.sde.edc.entities.UsagePolicy;
import com.catenax.sde.edc.enums.PolicyAccessEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class
QueryDataOfferModel {

	private String connectorId;

	private String assetId;

	private String offerId;
	
	private String connectorOfferUrl;
	
	private String title;

	private String version;

	private String description;

	private String fileName;

	private String fileContentType;

	private String created;

	private String modified;
	
	private String publisher;

	private PolicyAccessEnum typeOfAccess;

	private List<String> bpnNumbers;

	private String policyId;
	
	private List<UsagePolicy> usagePolicies;

}
