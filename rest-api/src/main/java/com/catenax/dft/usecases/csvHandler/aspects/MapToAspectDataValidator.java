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

package com.catenax.dft.usecases.csvHandler.aspects;

import com.catenax.dft.enums.OptionalIdentifierKeyEnum;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class MapToAspectDataValidator {

    protected final int ROW_LENGTH = 11;

    protected void validateAspectRowLength(String[] rowDataFields) {
        if (rowDataFields.length != ROW_LENGTH) {
            throw new MapToAspectException("This row has the wrong amount of fields");
        }
    }

    protected void validateAspectData(String[] rowDataFields) {
        String errorMessage = "";
        if (rowDataFields[1].isBlank()) {
            errorMessage = addEmptyFieldMessage(errorMessage, "part_instance_id");
        }
        if (rowDataFields[2].isBlank()) {
            errorMessage = addEmptyFieldMessage(errorMessage, "manufacturing_date");
        }
        if (rowDataFields[4].isBlank()) {
            errorMessage = addEmptyFieldMessage(errorMessage, "manufacturer_part_id");
        }
        if (rowDataFields[6].isBlank()) {
            errorMessage = addEmptyFieldMessage(errorMessage, "classification");
        }
        if (rowDataFields[7].isBlank()) {
            errorMessage = addEmptyFieldMessage(errorMessage, "name_at_manufacturer");
        }
        if (rowDataFields[9].isBlank() && !rowDataFields[10].isBlank()
                || !rowDataFields[9].isBlank() && rowDataFields[10].isBlank()) {
            errorMessage = addOptionalIdKeyMatchValueMessage(errorMessage);
        }

        if (!rowDataFields[9].isBlank() &&
                Stream.of(OptionalIdentifierKeyEnum.values())
                        .noneMatch(e -> e.getPrettyName()
                                .equalsIgnoreCase(rowDataFields[9]))) {
            errorMessage = addOptionalIdKeyTypeMessage(errorMessage);
        }

        if (!errorMessage.isBlank()) {
            throw new MapToAspectException(errorMessage);
        }
    }

    private String addEmptyFieldMessage(String errorMessage, String newMessage) {
        return errorMessage.isBlank() ? "Not allowed empty fields: " + newMessage : errorMessage + ", " + newMessage;
    }

    private String addOptionalIdKeyMatchValueMessage(String errorMessage) {
        String newMessage = "optional_identifier_key and optional_identifier_value have to be either both null or both filled";
        return errorMessage.isBlank() ? newMessage : errorMessage + " | " + newMessage;
    }

    private String addOptionalIdKeyTypeMessage(String errorMessage) {
        String newMessage = "illegal value for optional_identifier_key";
        return errorMessage.isBlank() ? newMessage : errorMessage + " | " + newMessage;
    }
}
