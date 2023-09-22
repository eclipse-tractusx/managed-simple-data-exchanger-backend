/********************************************************************************
 * Copyright (c) 2023 BMW GmbH
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.tractusx.sde.agent.entity.CsvUploadConfigEntity;
import org.eclipse.tractusx.sde.agent.repository.CsvUploadConfigRepository;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.sftp.dto.EmailNotificationModel;
import org.eclipse.tractusx.sde.sftp.dto.JobMaintenanceModel;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CsvUploadConfigurationService {

    private final CsvUploadConfigRepository repository;
    private final ObjectMapper mapper = new ObjectMapper();

    /* We are using this method to save any type of CsvUpload config using concrete type of config that we need */
    @SneakyThrows
    public String saveCsvUploadConfig(JsonNode config, ConfigType configType) {
        List<CsvUploadConfigEntity> notificationList = repository.findAllByType(configType.toString());
        CsvUploadConfigEntity configEntity;
        if (notificationList.isEmpty()) {
            configEntity = new CsvUploadConfigEntity();
            if(configType.equals(ConfigType.NOTIFICATION)) {
                configEntity.setUuid(CsvUploadConfigEntity.NOTIFICATION_CONFIG_ID);
            } else if (configType.equals(ConfigType.JOB_MAINTENANCE)) {
                configEntity.setUuid(CsvUploadConfigEntity.JOB_MAINTENANCE_CONFIG_ID);
            }
            configEntity.setType(configType.toString());
        } else {
            configEntity = notificationList.get(0);
        }

        try {
            configEntity.setContent(mapper.writeValueAsString(config));
        } catch (JsonProcessingException jpe) {
            throw new ServiceException("Error while getting the config");
        }

        repository.save(configEntity);
        return configEntity.getUuid();
    }

    @SneakyThrows
    public Object getCsvUploadConfig(ConfigType configType) {
        List<CsvUploadConfigEntity> notificationList = repository.findAllByType(configType.toString());
        if (notificationList.isEmpty()) return null;
        else {
            try {
                CsvUploadConfigEntity configEntity = notificationList.get(0);
                switch (configType) {
                    case NOTIFICATION -> {
                        return mapper.readValue(configEntity.getContent(), EmailNotificationModel.class);
                    }
                    case JOB_MAINTENANCE -> {
                        return mapper.readValue(configEntity.getContent(), JobMaintenanceModel.class);
                    }
                }
            } catch (JsonProcessingException jpe) {
                throw new ServiceException("Error while getting the config");
            }
        }
        return null;
    }
}
