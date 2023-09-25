/********************************************************************************
 * Copyright (c) 2023 T-Systems International GmbH
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.sftp.service;

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.SubmodelPolicyRequest;
import org.eclipse.tractusx.sde.core.policy.service.PolicyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class PolicyProvider {

	private final PolicyService policyService;

	@Value(value = "${manufacturerId}")
	private String bpnNumber;

	ObjectMapper mapper = new ObjectMapper();

	private String defaultPolicy = """
					{
					"policy_name": "default",
			        "bpn_numbers": [
			                    "%s"
			                ],
			                "type_of_access": "restricted",
			                "usage_policies": [
			                    {
			                        "type": "DURATION",
			                        "typeOfAccess": "UNRESTRICTED",
			                        "value": "",
			                        "durationUnit": "SECOND"
			                    },
			                    {
			                        "type": "ROLE",
			                        "typeOfAccess": "UNRESTRICTED",
			                        "value": ""
			                    },
			                    {
			                        "type": "PURPOSE",
			                        "typeOfAccess": "UNRESTRICTED",
			                        "value": ""
			                    },
			                    {
			                        "type": "CUSTOM",
			                        "typeOfAccess": "UNRESTRICTED",
			                        "value": ""
			                    }
			                ]
			            }
			""";

	@SneakyThrows
	public void saveDefaultPolicy() {
		if (policyService.getPolicyByName("default") == null) {
			SubmodelPolicyRequest policy = getDefaultPolicy();
			policyService.savePolicy(policy);

		}
	}

	@SneakyThrows
	public SubmodelPolicyRequest getDefaultPolicy() {
		String defaultPolicyStr = String.format(defaultPolicy, bpnNumber);
		return mapper.readValue(defaultPolicyStr, SubmodelPolicyRequest.class);
	}

	public SubmodelPolicyRequest getMatchingPolicyBasedOnFileName(String fileName) {
		List<SubmodelPolicyRequest> matchingList = policyService.findByPolicyNameLike("%" + fileName + "%");
		if (matchingList.isEmpty())
			return getDefaultPolicy();
		else
			return matchingList.get(0);
	}
}