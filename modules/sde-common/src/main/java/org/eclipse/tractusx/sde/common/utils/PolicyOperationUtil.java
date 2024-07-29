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
package org.eclipse.tractusx.sde.common.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.entities.Policies;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;

public class PolicyOperationUtil {

	private static final String BUSINESS_PARTNER_NUMBER = "BusinessPartnerNumber";

	private PolicyOperationUtil() {
	}

	public static List<String> getBPNList(List<Policies> policies) {
		return policies.stream().filter(e -> e.getTechnicalKey().equals(BUSINESS_PARTNER_NUMBER))
				.flatMap(e -> e.getValue().stream().filter(StringUtils::isNotBlank)).toList();
	}

	public static List<String> getAccessBPNList(PolicyModel policy) {
		return getBPNList(policy.getAccessPolicies());
	}

	public static List<String> getUsageBPNList(PolicyModel policy) {
		return getBPNList(policy.getUsagePolicies());
	}
	
	public static List<Policies> getStringPolicyAsPolicyList(String policyStr){
		
		List<Policies> policies = new ArrayList<>();
		
		if(StringUtils.isNotBlank(policyStr)) {
			String[] split = policyStr.split(";");
			for (int i = 0; i < split.length; i++) {
				String[] split1 = split[i].split("@");
				
				if (split1.length == 3) {
					policies.add(Policies.builder().technicalKey(split1[0]).operator(split1[1]).value(List.of(split1[2])).build());
				}
				
				else if (split1.length == 2) {
					policies.add(Policies.builder().technicalKey(split1[0]).value(List.of(split1[1])).build());
				}
			}
		}
		
		return policies;
	}
}