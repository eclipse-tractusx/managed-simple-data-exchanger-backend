/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.catenax.dft.validators;

import java.util.stream.Stream;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.catenax.dft.enums.OptionalIdentifierKeyEnum;

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
