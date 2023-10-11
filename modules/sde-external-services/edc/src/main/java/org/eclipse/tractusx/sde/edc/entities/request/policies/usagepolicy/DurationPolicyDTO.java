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
import org.eclipse.tractusx.sde.common.enums.DurationEnum;
import org.eclipse.tractusx.sde.common.enums.PolicyAccessEnum;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ConstraintRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.Operator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
public class DurationPolicyDTO extends UsagePolicyDTO {
	private DurationEnum durationUnit;

	public static DurationPolicyDTO fromUsagePolicy(UsagePolicies usagePolicy) {
		return DurationPolicyDTO.builder()
				.typeOfAccess(usagePolicy.getTypeOfAccess())
				.value(usagePolicy.getValue())
				.durationUnit(DurationEnum.valueOf(usagePolicy.getDurationUnit()))
				.build();

	}

	@Override
	public ConstraintRequest toConstraint() {
		if (getTypeOfAccess().equals(PolicyAccessEnum.RESTRICTED)) {

			String operator = "odrl:lteq";
			String value = "P";
			switch (this.durationUnit) {
			case YEAR:
				value = value + getValue() + "Y0M0DT0H0M0S";
				break;
			case MONTH:
				value = value + "0Y" + getValue() + "M0DT0H0M0S";
				break;
			case DAY:
				value = value + "0Y0M" + getValue() + "DT0H0M0S";
				break;
			case HOUR:
				value = value + "0Y0M0DT" + getValue() + "H0M0S";
				break;
			case MINUTE:
				value = value + "0Y0M0DT0H" + getValue() + "M0S";
				break;
			case SECOND:
				value = value + "0Y0M0DT0H0M" + getValue() + "S";
				break;
			default:
				break;
			}

			return ConstraintRequest.builder()
					.leftOperand("idsc:ELAPSED_TIME")
					.operator(Operator.builder().id(operator).build())
					.rightOperand(value)
					.build();

		}

		return null;
	}
}
