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

import com.catenax.dft.entities.SubmodelFileRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UploadFileUsagePolicyValidationService implements ConstraintValidator<UsagePolicyValidation, String> {

    private ObjectMapper objectMapper = new ObjectMapper();
    private final ValidationService validationService;

    public UploadFileUsagePolicyValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public void initialize(UsagePolicyValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @SneakyThrows
    @Override
    public boolean isValid(String metadata, ConstraintValidatorContext constraintValidatorContext) {
        SubmodelFileRequest submodelFileRequest = objectMapper.readValue(metadata, SubmodelFileRequest.class);
        return validationService.isValid(submodelFileRequest.getUsagePolicies());
    }
}
