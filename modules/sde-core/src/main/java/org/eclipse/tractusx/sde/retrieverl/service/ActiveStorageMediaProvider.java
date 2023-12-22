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

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.agent.ConfigService;
import org.eclipse.tractusx.sde.agent.model.ActiveStorageMedia;
import org.eclipse.tractusx.sde.common.ConfigurationProvider;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActiveStorageMediaProvider implements ConfigurationProvider<ActiveStorageMedia> {

	private final ConfigService configService;
	
	@Value("${retriever.active:}")
	private String retriever;

    private final ApplicationContext applicationContext;

	@PostConstruct
	public void init() {
        ActiveStorageMedia media = getConfiguration();
        if(media != null)
            log.info("There is a active storage media present: "+media.getName());
        else
            log.warn("There is no default active storage media");
	}
	
    @Override
    public ActiveStorageMedia getConfiguration() {
        return configService.getConfigurationAsObject(ActiveStorageMedia.class)
                .orElseGet(() -> {
                    var jmp = getDefaultActiveStorageMedia();
                    if(jmp != null)
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
        if(StringUtils.isNotBlank(retriever)) {
            try {
                applicationContext.getBean(retriever.toLowerCase());
            } catch(Exception e) {
                throw new ValidationException("To activate default storage media 'retriever.active="+ retriever +"' not supported");
            }
            return ActiveStorageMedia.builder().name(retriever).build();
        } else
            return null;
    }
}
