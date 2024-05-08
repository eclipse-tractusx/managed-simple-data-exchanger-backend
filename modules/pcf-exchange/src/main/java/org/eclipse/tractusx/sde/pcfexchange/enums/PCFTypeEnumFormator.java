/********************************************************************************
 * Copyright (c) 2024 T-Systems International GmbH
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.pcfexchange.enums;

import java.text.ParseException;
import java.util.Locale;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.Formatter;

public class PCFTypeEnumFormator implements Formatter<PCFTypeEnum> {
	
    @Override
    public String print(PCFTypeEnum object, Locale locale) {
        return null;
    }

    @Override
    public PCFTypeEnum parse(String text, Locale locale) throws ParseException {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        return EnumUtils.getEnum(PCFTypeEnum.class, text.toUpperCase());
    }
}