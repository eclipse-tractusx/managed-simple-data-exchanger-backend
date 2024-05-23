/********************************************************************************
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

package org.eclipse.tractusx.sde.edc.model.response;

import java.util.Objects;

import org.eclipse.tractusx.sde.common.entities.PolicyModel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class QueryDataOfferModel {

	private String connectorId;

	private String assetId;
	
	private String manufacturerPartId;

	private String offerId;

	private String connectorOfferUrl;

	private String title;

	private String type;

	private String version;
	
	private String sematicVersion;

	private String description;

	private String fileName;

	private String fileContentType;

	private String created;

	private String modified;

	private String publisher;

	private String policyId;

	private PolicyModel policy;
	
	private JsonNode hasPolicy;

	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;

		if (obj == null || obj.getClass() != this.getClass())
			return false;

		// type casting of the argument.
		QueryDataOfferModel offer = (QueryDataOfferModel) obj;

		return (offer.assetId.equals(this.assetId) 
				&& offer.connectorOfferUrl.equals(this.connectorOfferUrl)
				&& offer.sematicVersion.equals(this.sematicVersion));
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.assetId.hashCode());
	}

}
