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
package org.eclipse.tractusx.sde.core.policy.entity;

import java.util.Collections;
import java.util.List;

import org.eclipse.tractusx.sde.common.entities.Policies;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;

@Converter
public class PoliciesListToStringConverter implements AttributeConverter<List<Policies>, String> {

	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	@SneakyThrows
	public String convertToDatabaseColumn(List<Policies> attribute) {
		String policiesList = objectMapper.writeValueAsString(attribute);
		return attribute == null ? null : policiesList;
	}

	@Override
	@SneakyThrows
	public List<Policies> convertToEntityAttribute(String dbData) {
		return dbData == null ? Collections.emptyList()
				: objectMapper.readValue(dbData, new TypeReference<List<Policies>>() {
				});
	}
}
