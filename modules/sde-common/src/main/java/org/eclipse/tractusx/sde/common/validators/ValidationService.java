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

package org.eclipse.tractusx.sde.common.validators;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.entities.PolicyTemplateRequest;
import org.eclipse.tractusx.sde.common.entities.PolicyTemplateType;
import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.common.enums.DurationEnum;
import org.eclipse.tractusx.sde.common.enums.PolicyAccessEnum;
import org.eclipse.tractusx.sde.common.enums.UsagePolicyEnum;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidationService {

	public boolean isPolicyTemplateRequestValid(PolicyTemplateRequest policy,
			ConstraintValidatorContext constraintValidatorContext) {

		if (policy.getType().equals(PolicyTemplateType.NONE))
			return extracted(policy, constraintValidatorContext);
		else if (policy.getType().equals(PolicyTemplateType.NEW)) {
			boolean accesstype = checkAccessType(policy.getTypeOfAccess(), constraintValidatorContext);
			boolean policyCheck = extracted(policy, constraintValidatorContext);
			boolean policyName = policyName(policy.getPolicyName(), constraintValidatorContext);
			return policyCheck && accesstype && policyName;
		} else if (StringUtils.isBlank(policy.getUuid())) {
			constraintValidatorContext.buildConstraintViolationWithTemplate("Policy UUID must not be null or empty")
					.addPropertyNode("uuid").addConstraintViolation().disableDefaultConstraintViolation();
			return false;
		}

		return true;
	}

	private boolean extracted(PolicyTemplateRequest policy, ConstraintValidatorContext constraintValidatorContext) {
		boolean access = accessPolicyValidation(policy.getBpnNumbers(), constraintValidatorContext);
		boolean usage = usagePolicyValidation(policy.getUsagePolicies(), constraintValidatorContext);
		return access && usage;
	}

	public boolean policyName(String name, ConstraintValidatorContext constraintValidatorContext) {
		boolean isValid = true;
		if (StringUtils.isBlank(name)) {
			constraintValidatorContext.buildConstraintViolationWithTemplate("Policy name must not be null or empty")
					.addPropertyNode("policyName").addConstraintViolation().disableDefaultConstraintViolation();

			isValid = false;
		}
		return isValid;
	}

	public boolean accessPolicyValidation(List<String> bpnNumbers,
			ConstraintValidatorContext constraintValidatorContext) {
		boolean isValid = true;
		if (CollectionUtils.isEmpty(bpnNumbers)) {
			isValid = false;
			constraintValidatorContext.buildConstraintViolationWithTemplate("Access policy must not be null or empty")
					.addPropertyNode("bpnNumbers").addConstraintViolation().disableDefaultConstraintViolation();
		}

		return isValid;
	}

	public boolean usagePolicyValidation(Map<UsagePolicyEnum, UsagePolicies> usagePolicies,
			ConstraintValidatorContext constraintValidatorContext) {
		boolean isValid = true;
		if (!CollectionUtils.isEmpty(usagePolicies)) {
			boolean validateFlag = false;
			for (Map.Entry<UsagePolicyEnum, UsagePolicies> entry : usagePolicies.entrySet()) {
				validateFlag = validatePolicy(entry, constraintValidatorContext);
				if (validateFlag && entry.getKey().equals(UsagePolicyEnum.DURATION)) {
					validateFlag = validateDuration(entry.getValue(), constraintValidatorContext);
				}
				if (isValid)
					isValid = validateFlag;
			}
		} else {
			isValid = false;
			constraintValidatorContext.buildConstraintViolationWithTemplate("Usage Policy must not be null or empty")
					.addPropertyNode("usagePolicies").addConstraintViolation().disableDefaultConstraintViolation();
		}

		return isValid;

	}

	private boolean validatePolicy(Map.Entry<UsagePolicyEnum, UsagePolicies> entry,
			ConstraintValidatorContext constraintValidatorContext) {

		boolean isValid = true;
		if (entry.getValue().getTypeOfAccess().equals(PolicyAccessEnum.RESTRICTED)
				&& StringUtils.isBlank(entry.getValue().getValue())) {
			isValid = false;
			constraintValidatorContext.buildConstraintViolationWithTemplate("value must not be null or empty")
					.addPropertyNode("usagePolicies." + entry.getKey()).addConstraintViolation()
					.disableDefaultConstraintViolation();
		}

		return isValid;
	}

	private boolean validateDuration(UsagePolicies usagePolicy, ConstraintValidatorContext constraintValidatorContext) {
		boolean isValid = true;
		List<String> result = Stream.of(DurationEnum.values()).map(Enum::name).toList();
		if (!result.contains(usagePolicy.getDurationUnit())) {
			isValid = false;
			constraintValidatorContext
					.buildConstraintViolationWithTemplate("durationUnit '" + usagePolicy.getDurationUnit()
							+ "' not one of the values accepted for Enum class " + result)
					.addPropertyNode("usagePolicies.DURATION").addConstraintViolation()
					.disableDefaultConstraintViolation();
		}

		return isValid;
	}

	public boolean checkAccessType(String typeOfAccess, ConstraintValidatorContext constraintValidatorContext) {
		boolean isValid = true;
		if (StringUtils.isBlank(typeOfAccess)) {
			isValid = false;
			constraintValidatorContext.buildConstraintViolationWithTemplate("Access type should not be null or empty")
					.addPropertyNode("typeOfAccess").addConstraintViolation().disableDefaultConstraintViolation();
		}

		return isValid;
	}

}