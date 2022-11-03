/********************************************************************************
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the CatenaX (ng) GitHub Organisation
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DateValidator implements ConstraintValidator<DateValidation, String> {

	@Override
	public boolean isValid(String input, ConstraintValidatorContext cxt) {
		String strMessage = "";
		try {
			
			cxt.disableDefaultConstraintViolation();
			if (input.contains("Z")) {
				strMessage = "assembledOn";
				Instant.parse(input);
				return true;
			} else {
				final String DATE_PATTERN = "uuuu-M-d'T'HH:mm:ss";
				strMessage = "manufacturing";
				LocalDate.parse(input,
						DateTimeFormatter.ofPattern(DATE_PATTERN).withResolverStyle(ResolverStyle.STRICT));
				return true;
			}
		} catch (Exception e) {
			cxt.buildConstraintViolationWithTemplate("Invalid  " + strMessage + "Date!").addConstraintViolation();
			return false;
		}
	}
}