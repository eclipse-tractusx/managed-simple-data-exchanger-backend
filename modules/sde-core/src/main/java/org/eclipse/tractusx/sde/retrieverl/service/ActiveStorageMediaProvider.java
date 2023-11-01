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

package org.eclipse.tractusx.sde.retrieverl.service;

import org.eclipse.tractusx.sde.agent.ConfigService;
import org.eclipse.tractusx.sde.agent.model.ActiveStorageMedia;
import org.eclipse.tractusx.sde.common.ConfigurationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActiveStorageMediaProvider implements ConfigurationProvider<ActiveStorageMedia> {

	private final ConfigService configService;
	
	@Value("${retriever.active:Minio}")
	private String retriever;

	@PostConstruct
	public void init() {
		saveConfig(getConfiguration());
	}
	
    @Override
    public ActiveStorageMedia getConfiguration() {
        return configService.getConfigurationAsObject(ActiveStorageMedia.class)
                .orElseGet(() -> {
                    var jmp = getDefaultActiveStorageMedia();
                    saveConfig(jmp);
                    return jmp;
                });
    }

    @Override
    public void saveConfig(ActiveStorageMedia config) {
        configService.saveConfiguration(config);
    }

    @Override
    public Class<ActiveStorageMedia> getConfigClass() {
        return ActiveStorageMedia.class;
    }

    private ActiveStorageMedia getDefaultActiveStorageMedia() {
        return ActiveStorageMedia.builder().name(retriever).build();
    }
}
