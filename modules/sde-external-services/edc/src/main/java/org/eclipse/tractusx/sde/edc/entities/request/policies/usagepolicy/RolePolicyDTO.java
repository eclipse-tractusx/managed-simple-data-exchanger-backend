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

package org.eclipse.tractusx.sde.edc.entities.request.policies.usagepolicy;

import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.common.enums.PolicyAccessEnum;
import org.eclipse.tractusx.sde.common.enums.UsagePolicyEnum;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ConstraintRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.Operator;

import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@SuperBuilder
public class RolePolicyDTO extends UsagePolicyDTO {

	public static RolePolicyDTO fromUsagePolicy(UsagePolicies usagePolicy) {
		return RolePolicyDTO.builder().type(UsagePolicyEnum.ROLE).typeOfAccess(usagePolicy.getTypeOfAccess())
				.value(usagePolicy.getValue()).build();

	}

	@Override
	public ConstraintRequest toConstraint() {
		if (getTypeOfAccess().equals(PolicyAccessEnum.RESTRICTED)) {

			String operator = "odrl:eq";

			return ConstraintRequest.builder().leftOperand("idsc:ROLE")
					.operator(Operator.builder().id(operator).build()).rightOperand(getValue()).build();

		}
		return null;
	}
}
