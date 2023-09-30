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

import org.eclipse.tractusx.sde.common.entities.PolicyTemplateRequest;
import org.eclipse.tractusx.sde.common.entities.SubmodelJsonRequest;
import org.springframework.stereotype.Service;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Service
public class PolicyTemplateValidationAsPojo
		implements ConstraintValidator<ValidatePolicyTemplate, SubmodelJsonRequest> {

	private final ValidationService validationService;

	public PolicyTemplateValidationAsPojo(ValidationService validationService) {
		this.validationService = validationService;
	}

	@Override
	public void initialize(ValidatePolicyTemplate constraintAnnotation) {
		ConstraintValidator.super.initialize(constraintAnnotation);
	}

	@Override
	public boolean isValid(SubmodelJsonRequest obj, ConstraintValidatorContext constraintValidatorContext) {

		PolicyTemplateRequest policyTemplateRequest = PolicyTemplateRequest.builder().type(obj.getType())
				.policyName(obj.getPolicyName()).bpnNumbers(obj.getBpnNumbers()).typeOfAccess(obj.getTypeOfAccess())
				.usagePolicies(obj.getUsagePolicies()).uuid(obj.getUuid()).build();

		return validationService.isPolicyTemplateRequestValid(policyTemplateRequest, constraintValidatorContext);
	}
}
