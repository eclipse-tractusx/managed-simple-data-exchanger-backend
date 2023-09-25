/********************************************************************************
 * Copyright (c) 2023 T-Systems International GmbH
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.sftp.service;

import org.eclipse.tractusx.sde.agent.entity.ConfigEntity;
import org.eclipse.tractusx.sde.agent.mapper.AutoUploadAgentConfigMapper;
import org.eclipse.tractusx.sde.agent.model.ConfigResponse;
import org.eclipse.tractusx.sde.agent.model.ConfigType;
import org.eclipse.tractusx.sde.agent.repository.AutoUploadAgentConfigRepository;
import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class AutoUploadAgentConfigService {

	private final AutoUploadAgentConfigRepository repository;

	private final AutoUploadAgentConfigMapper configMapper;

	ObjectMapper mapper = new ObjectMapper();

	@SneakyThrows
	public JsonNode saveConfiguration(ConfigType configType, Object configObject) {
		ConfigEntity configEntity = new ConfigEntity();
		configEntity.setType(configType.toString());
		configEntity.setContent(mapper.writeValueAsString(configObject));
		repository.save(configEntity);
		log.info(configType + " configuration save in database");
		return mapper.readValue(configMapper.mapFrom(configEntity).getContent(), JsonNode.class);
	}

	@SneakyThrows
	public ConfigResponse getConfigurationAsObject(ConfigType configType) {
		return configMapper.mapFrom(repository.findAllByType(configType.toString())
				.orElseThrow(() -> new NoDataFoundException("No data found type " + configType.name())));

	}

	@SneakyThrows
	public JsonNode getConfiguration(ConfigType configType) {
		ConfigResponse response = getConfigurationAsObject(configType);
		return mapper.readValue(response.getContent(), JsonNode.class);
	}

}
