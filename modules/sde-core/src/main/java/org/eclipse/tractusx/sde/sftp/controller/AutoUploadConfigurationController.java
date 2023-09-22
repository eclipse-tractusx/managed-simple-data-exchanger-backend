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

package org.eclipse.tractusx.sde.sftp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.sde.agent.model.SchedulerConfigModel;
import org.eclipse.tractusx.sde.agent.model.SftpConfigModel;
import org.eclipse.tractusx.sde.sftp.dto.EmailNotificationModel;
import org.eclipse.tractusx.sde.sftp.dto.JobMaintenanceModel;
import org.eclipse.tractusx.sde.sftp.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class AutoUploadConfigurationController {

    private final MetadataProvider metadataProvider;
    private final SftpRetrieverFactoryImpl sftpRetrieverFactory;
    private final SchedulerService schedulerService;
    private final CsvUploadConfigurationService csvUploadConfirationService;


    @PutMapping("/schedulerConfig")
    public String updateScheduler(@NotBlank @RequestBody SchedulerConfigModel schedulerConfig) {
        return schedulerService.updateScheduler(schedulerConfig);
    }

    @GetMapping("/schedulerConfig")
    public SchedulerConfigModel getSchedulerConfig() {
        return schedulerService.getCurrentSchedule();
    }

    @PostMapping("/sdeAgentConfig")
    public String updateSftpConfig(@NotBlank @RequestBody JsonNode config,
                                   @RequestParam("type") ConfigType type) {
        if (type.equals(ConfigType.METADATA)) {
            return metadataProvider.saveMetadata(config);
        } else if (type.equals(ConfigType.CLIENT)) {
            return sftpRetrieverFactory.saveConfig(config);
        }
        return "Unsupported type";
    }

    @GetMapping("/sftpMetadata")
    public JsonNode getSftpMetadata() {
        return metadataProvider.getMetadata();
    }


    @GetMapping("/sftpConfig")
    public SftpConfigModel getSftpConfig() {
        return sftpRetrieverFactory.getConfig();
    }

    @PostMapping("/notification")
    public String updateNotificationConfig(@NotBlank @RequestBody JsonNode config) throws JsonProcessingException {
        return csvUploadConfirationService.saveCsvUploadConfig(config, ConfigType.NOTIFICATION);
    }

    @GetMapping("/notification")
    public EmailNotificationModel getNotificationConfig()  {
        return Optional.ofNullable((EmailNotificationModel)csvUploadConfirationService.getCsvUploadConfig(ConfigType.NOTIFICATION))
                .orElse(new EmailNotificationModel());
    }

    @PostMapping("/job-maintenance")
    public String updateJobMaintenanceConfig(@NotBlank @RequestBody JsonNode config){
        return csvUploadConfirationService.saveCsvUploadConfig(config, ConfigType.JOB_MAINTENANCE);
    }

    @GetMapping("/job-maintenance")
    public ResponseEntity<JobMaintenanceModel> getJobMaintenanceConfig() {
        JobMaintenanceModel res = (JobMaintenanceModel)csvUploadConfirationService.getCsvUploadConfig(ConfigType.JOB_MAINTENANCE);
        if(res != null) {return ok().body(res);}
        else {
            return ResponseEntity.notFound().build();
        }
    }


}
