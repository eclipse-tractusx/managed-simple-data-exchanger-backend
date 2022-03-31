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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.catenax.dft.entities.usecases;

import com.catenax.dft.validators.QuantityNumberValidation;
import lombok.Builder;
import lombok.Data;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Builder
@Data
public class ChildAspect {

    @Nullable
    private String uuid;

    private String processId;

    @NotBlank(message = "parent_identifier_key cannot be empty")
    private String parentIdentifierKey;

    @NotBlank(message = "parent_identifier_value cannot be empty")
    private String parentIdentifierValue;

    @NotBlank(message = "lifecycle_context cannot be empty")
    private String lifecycleContext;

    @QuantityNumberValidation
    private String quantityNumber;

    @NotBlank(message = "measurement_unit_lexical_value cannot be empty")
    private String measurementUnitLexicalValue;
}