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

package org.eclipse.tractusx.sde.common.validators;

import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.springframework.stereotype.Service;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsagePolicyValidator implements ConstraintValidator<ValidatePolicyTemplate, PolicyModel> {

	private final ValidationService validationService;

	@Override
	public void initialize(ValidatePolicyTemplate constraintAnnotation) {
		ConstraintValidator.super.initialize(constraintAnnotation);
	}

	public boolean isValid(PolicyModel policy, ConstraintValidatorContext constraintValidatorContext) {
		boolean policyName = validationService.policyName(policy.getPolicyName(), constraintValidatorContext);
		boolean access = validationService.accessPoliciesValidation(policy.getAccessPolicies(), constraintValidatorContext);
		boolean usage = validationService.usagePoliciesValidation(policy.getUsagePolicies(), constraintValidatorContext);
		return policyName && access && usage;
	}

}
