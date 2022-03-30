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

import com.catenax.dft.enums.OptionalIdentifierKeyEnum;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.stream.Stream;

public class OptionalIdentifierKeyValidator implements ConstraintValidator<OptionalIdentifierKeyValidation, String> {

    @Override
    public boolean isValid(String input, ConstraintValidatorContext cxt) {
        try {
            return input == null
                    || Stream.of(OptionalIdentifierKeyEnum.values())
                    .anyMatch(e -> e.getPrettyName().equalsIgnoreCase(input));
        } catch (Exception e) {
            return false;
        }
    }
}
