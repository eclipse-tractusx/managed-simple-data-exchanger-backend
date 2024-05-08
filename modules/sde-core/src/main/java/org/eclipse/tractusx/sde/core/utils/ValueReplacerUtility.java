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
package org.eclipse.tractusx.sde.core.utils;

import java.io.InputStream;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingJsonFactory;

import lombok.SneakyThrows;

@Component
public class ValueReplacerUtility {

	@SneakyThrows
	public String getRequestFile(String schemaFile) {
		JsonParser createParser = null;
		String schema = null;
		try {
			MappingJsonFactory jf = new MappingJsonFactory();
			InputStream jsonFile = this.getClass().getResourceAsStream(schemaFile);

			if (jsonFile == null) {
				// this is how we load file within editor (eg eclipse)
				jsonFile = this.getClass().getClassLoader().getResourceAsStream(schemaFile);
			}
			createParser = jf.createParser(jsonFile);
			schema = createParser.readValueAsTree().toString();
			if (schema == null) {
				throw new ServiceException("The schema file is null " + schemaFile);
			}

			return schema;
		} finally {
			if (createParser != null)
				createParser.close();
		}
	}

	@SneakyThrows
	public String valueReplacer(String requestTemplate, Map<String, String> inputData) {
		StringSubstitutor stringSubstitutor1 = new StringSubstitutor(inputData);
		return stringSubstitutor1.replace(requestTemplate);
	}
	
	@SneakyThrows
	public String valueReplacerUsingFileTemplate(String requestTemplatePath, Map<String, String> inputData) {
		StringSubstitutor stringSubstitutor1 = new StringSubstitutor(inputData);
		return stringSubstitutor1.replace(getRequestFile(requestTemplatePath));
	}

}