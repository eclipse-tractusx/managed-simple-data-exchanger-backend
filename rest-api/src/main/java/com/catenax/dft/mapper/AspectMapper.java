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

package com.catenax.dft.mapper;

import com.catenax.dft.entities.database.AspectEntity;
import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.enums.OptionalIdentifierKeyEnum;
import com.catenax.dft.usecases.csvHandler.exceptions.MapToAspectException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public abstract class AspectMapper {

    @Mapping(target = "rowNumber", ignore = true)
    public abstract mapFrom(AspectEntity aspect);

    @Mapping(target = "optionalIdentifierKey", expression = "java(convertToEnum(aspect.getOptionalIdentifierKey()))")

    public abstract AspectEntity mapFrom(Aspect aspect);

    protected OptionalIdentifierKeyEnum convertToEnum(String string) {

        if (string == null) return null;
        switch (string.toUpperCase()) {
            case "VAN":
                return OptionalIdentifierKeyEnum.VAN;
            case "BATCHID":
                return OptionalIdentifierKeyEnum.BATCH_ID;
            default:
                throw new MapToAspectException("illegal value for optional_identifier_key");
        }
    }
}
