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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.entities.usecases.Batch;

public class BatchValidator implements ConstraintValidator<SubmodelValidation, Batch> {
    @Override
    public void initialize(SubmodelValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Batch batch, ConstraintValidatorContext constraintValidatorContext) {
        if (!(batch instanceof Batch)) {
            throw new IllegalArgumentException("@BatchValidation only applies to batch objects");
        }

        String optionalIdentifierKey = batch.getOptionalIdentifierKey();
        String optionalIdentifierValue = batch.getOptionalIdentifierValue();

        return optionalIdentifierKey == null && optionalIdentifierValue == null
                || optionalIdentifierKey != null && optionalIdentifierValue != null;
    }
}
