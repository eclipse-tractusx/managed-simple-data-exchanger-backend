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

package com.catenax.dft.enums;


import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class OptionalIdentifierKeyEnumConverter implements AttributeConverter<OptionalIdentifierKeyEnum, String> {

    @Override
    public String convertToDatabaseColumn(OptionalIdentifierKeyEnum optionalIdentifierKeyEnum) {
        if (optionalIdentifierKeyEnum == null) {
            return null;
        }
        return optionalIdentifierKeyEnum.getPrettyName();
    }

    @Override
    public OptionalIdentifierKeyEnum convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }

        return Stream.of(OptionalIdentifierKeyEnum.values())
                .filter(c -> c.getPrettyName().equals(s))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

}
