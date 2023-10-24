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

package org.eclipse.tractusx.sde.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.sde.agent.entity.ConfigEntity;
import org.eclipse.tractusx.sde.agent.repository.AutoUploadAgentConfigRepository;
import org.eclipse.tractusx.sde.common.utils.TryUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class ConfigService {

	private final AutoUploadAgentConfigRepository repository;

	ObjectMapper mapper = new ObjectMapper();

	@SneakyThrows
	public void saveConfiguration(Object configObject) {
		ConfigEntity configEntity = new ConfigEntity();
		configEntity.setType(configObject.getClass().getCanonicalName());
		configEntity.setContent(mapper.writeValueAsString(configObject));
		repository.save(configEntity);
		log.info("The " + configObject.getClass().getCanonicalName() + " configuration save in database");
	}

	public <T> Optional<T> getConfigurationAsObject(Class<T> tClass) {
		return repository.findByType(tClass.getCanonicalName())
				.map(ConfigEntity::getContent)
				.flatMap(d -> TryUtils.tryExec(() -> mapper.readValue(d, tClass), TryUtils.IGNORE()));
	}

	@SneakyThrows
	public void deleteAllConfig() {
		repository.deleteAll();
	}
}
