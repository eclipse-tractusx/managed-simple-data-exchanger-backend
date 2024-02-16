/********************************************************************************
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

package org.eclipse.tractusx.sde.common.validators;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.entities.Policies;
import org.eclipse.tractusx.sde.common.entities.PolicyTemplateRequest;
import org.eclipse.tractusx.sde.common.entities.PolicyTemplateType;
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
			boolean policyCheck = extracted(policy, constraintValidatorContext);
			boolean policyName = policyName(policy.getPolicyName(), constraintValidatorContext);
			return policyCheck && policyName;
		} else if (StringUtils.isBlank(policy.getUuid())) {
			constraintValidatorContext.buildConstraintViolationWithTemplate("Policy UUID must not be null or empty")
					.addPropertyNode("uuid").addConstraintViolation().disableDefaultConstraintViolation();
			return false;
		}

		return true;
	}

	private boolean extracted(PolicyTemplateRequest policy, ConstraintValidatorContext constraintValidatorContext) {
		boolean access = accessPoliciesValidation(policy.getAccessPolicies(), constraintValidatorContext);
		boolean usage = usagePoliciesValidation(policy.getUsagePolicies(), constraintValidatorContext);
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

	public boolean accessPoliciesValidation(List<Policies> accessPoliciesList,
			ConstraintValidatorContext constraintValidatorContext) {
		boolean isValid = true;
		if (CollectionUtils.isEmpty(accessPoliciesList)) {
			isValid = false;
			constraintValidatorContext.buildConstraintViolationWithTemplate("Access policy must not be null or empty")
					.addPropertyNode("accessPolicies").addConstraintViolation().disableDefaultConstraintViolation();
		}

		return isValid;
	}

	public boolean usagePoliciesValidation(List<Policies> usagePolicies,
			ConstraintValidatorContext constraintValidatorContext) {
		boolean isValid = true;
		if (CollectionUtils.isEmpty(usagePolicies)) {
			isValid = false;
			constraintValidatorContext.buildConstraintViolationWithTemplate("usage policy must not be null or empty")
					.addPropertyNode("usagePolicies").addConstraintViolation().disableDefaultConstraintViolation();
			} 
		return isValid;

	}
}