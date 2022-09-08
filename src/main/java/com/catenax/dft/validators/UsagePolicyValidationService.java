/*
 * Copyright 2022 CatenaX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.catenax.dft.validators;

import com.catenax.dft.entities.UsagePolicyRequest;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

@Service
public class UsagePolicyValidationService implements ConstraintValidator<UsagePolicyValidation, List<UsagePolicyRequest>> {

    private final ValidationService validationService;

    public UsagePolicyValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public void initialize(UsagePolicyValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(List<UsagePolicyRequest> usagePolicyRequests, ConstraintValidatorContext constraintValidatorContext) {
        return validationService.isValid(usagePolicyRequests);
    }
}
