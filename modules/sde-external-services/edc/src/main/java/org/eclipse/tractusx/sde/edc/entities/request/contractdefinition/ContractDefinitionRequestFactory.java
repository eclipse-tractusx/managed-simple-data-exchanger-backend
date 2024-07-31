/********************************************************************************
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022, 2024 T-Systems International GmbH
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.edc.entities.request.contractdefinition;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ContractDefinitionRequestFactory {

	public ContractDefinitionRequest getContractDefinitionRequest(String uuid, String assetId, String accessPolicyId,
			String usagePolicyId) {
		
		String submodelId = uuid;
		if (submodelId.indexOf("urn:uuid") != -1) {
			submodelId = submodelId.substring(submodelId.indexOf("urn:uuid", 9));
			submodelId =submodelId.replace("urn:uuid:", "");
		}
		
		List<Criterion> criteria = new ArrayList<>();
		criteria.add(Criterion.builder()
				.operandLeft("https://w3id.org/edc/v0.0.1/ns/id")
				.operator("=")
				.operandRight(assetId)
				.build());
		
		return ContractDefinitionRequest.builder()
				.contractPolicyId(usagePolicyId == null ? accessPolicyId : usagePolicyId)
				.accessPolicyId(accessPolicyId)
				.id(submodelId)
				.assetsSelector(criteria)
				.build();
	}

}
