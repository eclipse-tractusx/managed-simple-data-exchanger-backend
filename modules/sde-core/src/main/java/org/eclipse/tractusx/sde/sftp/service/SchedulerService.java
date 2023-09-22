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
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.tractusx.sde.agent.entity.CsvUploadConfigEntity;
import org.eclipse.tractusx.sde.agent.model.SchedulerConfigModel;
import org.eclipse.tractusx.sde.agent.model.SchedulerType;
import org.eclipse.tractusx.sde.agent.repository.CsvUploadConfigRepository;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.eclipse.tractusx.sde.agent.entity.CsvUploadConfigEntity.SCHEDULER_CONFIG_ID;

@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final CsvUploadConfigRepository csvUploadConfigRepository;
    private final RetrieverScheduler retrieverScheduler;
    private final ObjectMapper mapper = new ObjectMapper();


    @EventListener(ApplicationReadyEvent.class)
    public void saveDefaultScheduler() throws JsonProcessingException {
        List<CsvUploadConfigEntity> configList = csvUploadConfigRepository.findAllByType(ConfigType.SCHEDULER.toString());
        if (configList == null || configList.isEmpty()) {
            SchedulerConfigModel schedulerConfigModel =  SchedulerConfigModel.builder()
                    .type(SchedulerType.HOURLY)
                    .time("1")
                    .build();

            CsvUploadConfigEntity configEntity = new CsvUploadConfigEntity();
            configEntity.setUuid(SCHEDULER_CONFIG_ID);
            configEntity.setType(ConfigType.SCHEDULER.toString());
            configEntity.setContent(mapper.writeValueAsString(schedulerConfigModel));

            csvUploadConfigRepository.save(configEntity);

            // Start the scheduler
            retrieverScheduler.schedule(convertScheduleToCron(schedulerConfigModel));
        } else {
            SchedulerConfigModel model = mapper.readValue(configList.get(0).getContent(), SchedulerConfigModel.class);
            retrieverScheduler.schedule(convertScheduleToCron(model));
        }
    }

    @SneakyThrows
    public String updateScheduler(SchedulerConfigModel schedulerConfig) {
        Optional<CsvUploadConfigEntity> optional = csvUploadConfigRepository.findById(SCHEDULER_CONFIG_ID);
        CsvUploadConfigEntity configEntity;
        if (optional.isPresent()) {
            configEntity = optional.get();
        } else {
            configEntity = new CsvUploadConfigEntity();
            configEntity.setUuid(SCHEDULER_CONFIG_ID);
            configEntity.setType(ConfigType.SCHEDULER.toString());
        }
        try {
            configEntity.setContent(mapper.writeValueAsString(schedulerConfig));
        } catch (JsonProcessingException e) {
            throw new ServiceException("Issue with the update scheduler config");
        }
        csvUploadConfigRepository.save(configEntity);
        retrieverScheduler.schedule(convertScheduleToCron(schedulerConfig));
        return configEntity.getUuid();
    }


    @SneakyThrows
    public SchedulerConfigModel getCurrentSchedule() {
        List<CsvUploadConfigEntity> configList = csvUploadConfigRepository.findAllByType(ConfigType.SCHEDULER.toString());
        if(configList != null && !configList.isEmpty()) {
            SchedulerConfigModel schedulerConfigModel = mapper.readValue(configList.get(0).getContent(), SchedulerConfigModel.class);
            return schedulerConfigModel;
        } else {
            throw new ServiceException("Scheduler config not found");
        }
    }



    public String convertScheduleToCron(SchedulerConfigModel model) {
        switch (model.getType()) {
            case DAILY -> {
                return "0 0 " + model.getTime() + " * * *";
            }
            case HOURLY -> {
                return "0 0 0/" + model.getTime() + " * * *";
            }
            case WEEKLY -> {
                return "0 0 " + model.getTime() + " * *" + model.getDay();
            }
            default -> {
                return "0 0 * * * *";
            }
        }
    }
}
