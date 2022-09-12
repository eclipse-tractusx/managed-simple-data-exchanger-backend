/********************************************************************************
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

package com.catenax.dft.usecases.common;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

import org.springframework.stereotype.Service;

import com.catenax.dft.usecases.csvhandler.exceptions.CsvHandlerUseCaseException;

@Service
public class DftDateValidator {
	private static final String DATE_PATTERN = "uuuu-M-d'T'HH:mm:ss";

	/**
	 * In getIfValidDateTime method we are validating date and time
	 * 
	 * @param strDateTime
	 * @param position
	 * @return
	 * @throws CsvHandlerUseCaseException
	 */
	public String getIfValidDateTime(String strDateTime, int position) throws CsvHandlerUseCaseException {

		try {
			LocalDate.parse(strDateTime,
					DateTimeFormatter.ofPattern(DATE_PATTERN).withResolverStyle(ResolverStyle.STRICT));
			return strDateTime;

		} catch (Exception ex) {
			throw new CsvHandlerUseCaseException(position, "Invalid Manufacturing Date");
		}

	}

	/**
	 * In getIfValidDateTimeForAssembly method we are validating date and time
	 * 
	 * @param strDateTime
	 * @param position
	 * @return
	 * @throws CsvHandlerUseCaseException
	 */
	public String getIfValidDateTimeForAssembly(String strDateTime, int position) throws CsvHandlerUseCaseException {

		try {
			// Note: using Instant which is having Zoned  DATE_PATTERN_FOR_ASSEMBLY = "uuuu-M-d'T'HH:mm:ss'Z'"
			 Instant.parse(strDateTime);
			 return strDateTime;

		} catch (Exception ex) {
			throw new CsvHandlerUseCaseException(position, "Invalid Assembled Date");
		}

	}

}
