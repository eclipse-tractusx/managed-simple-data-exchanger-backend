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

import com.catenax.dft.entities.usecases.Aspect;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class AspectValidator implements ConstraintValidator<AspectValidation, Aspect> {
    @Override
    public void initialize(AspectValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Aspect aspect, ConstraintValidatorContext constraintValidatorContext) {
        if (!(aspect instanceof Aspect)) {
            throw new IllegalArgumentException("@AspectValidation only applies to Aspect objects");
        }

        String optionalIdentifierKey = aspect.getOptionalIdentifierKey();
        String optionalIdentifierValue = aspect.getOptionalIdentifierValue();

        return optionalIdentifierKey == null && optionalIdentifierValue == null
                || optionalIdentifierKey != null && optionalIdentifierValue != null;
    }
}
