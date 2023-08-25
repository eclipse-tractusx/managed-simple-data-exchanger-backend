/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022, 2023 T-Systems International GmbH
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
import jakarta.validation.constraints.NotBlank;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.eclipse.tractusx.sde.agent.entity.SftpConfig;
import org.eclipse.tractusx.sde.agent.entity.SftpMetadataEntity;
import org.eclipse.tractusx.sde.agent.model.SftpConfigModel;
import org.eclipse.tractusx.sde.agent.repository.SftpConfigRepository;
import org.eclipse.tractusx.sde.agent.repository.SftpMetadataRepository;
import org.eclipse.tractusx.sde.agent.repository.SftpReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class FtpsService {

    @Autowired
    private SftpConfigModel sftpConfigModel;

    @Autowired
    private FTPClient ftpsClient;

    @Autowired
    private SftpReportRepository sftpReportRepository;

    @Autowired
    private SftpMetadataRepository sftpMetadataRepository;

    @Autowired
    private SftpConfigRepository sftpConfigRepository;

    private ObjectMapper objectMapper;

    @SneakyThrows
    public boolean connectFtpsClient() {
        disconnectFtpClient();
        log.info("Login to ftps server");
        ftpsClient.connect(sftpConfigModel.getUrl());
        int reply = ftpsClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftpsClient.disconnect();
            throw new IOException("Exception in connecting to FTP Server");
        }
        return ftpsClient.login(sftpConfigModel.getUsername(), sftpConfigModel.getPassword());
    }

    @SneakyThrows
    public void disconnectFtpClient() {
        log.info("Ftps client disconnected");
        ftpsClient.disconnect();
    }

    public void updateMetadata(@NotBlank Map<String, Object> metadataDto) throws JsonProcessingException {
        List<SftpMetadataEntity> sftpMetadataEntities = sftpMetadataRepository.findAll();
        log.debug("Updating ftps metadata");
        if (sftpMetadataEntities.size() > 1) {
            SftpMetadataEntity metadata = sftpMetadataEntities.get(0);
            String metadataContent = objectMapper.writeValueAsString(metadataDto);
            metadata.setContent(metadataContent);
            sftpMetadataRepository.save(metadata);
        } else {
            SftpMetadataEntity metadata = new SftpMetadataEntity();
            String metadataContent = objectMapper.writeValueAsString(metadataDto);
            metadata.setUuid(UUID.randomUUID().toString());
            metadata.setContent(metadataContent);
            sftpMetadataRepository.save(metadata);
        }
    }

    public void updateFtpsConfig(@NotBlank SftpConfigModel sftpConfigDto) {
        List<SftpConfig> sftpConfigs = sftpConfigRepository.findAll();
        log.debug("Updating ftps config");
        if (sftpConfigs.size() > 1) {
            SftpConfig config = sftpConfigs.get(0);
            sftpConfigRepository.save(mapDtoToEntity(sftpConfigDto, config));
        } else {
            SftpConfig config = new SftpConfig();
            sftpConfigRepository.save(mapDtoToEntity(sftpConfigDto, config));
        }
    }

    private SftpConfig mapDtoToEntity(SftpConfigModel sftpConfigDto, SftpConfig sftpConfig) {
        sftpConfig.setFailedLocation(sftpConfigDto.getFailedLocation());
        sftpConfig.setUrl(sftpConfigDto.getUrl());
        sftpConfig.setPassword(sftpConfigDto.getPassword());
        sftpConfig.setUsername(sftpConfigDto.getUsername());
        sftpConfig.setSuccessLocation(sftpConfigDto.getSuccessLocation());
        sftpConfig.setInProgressLocation(sftpConfigDto.getInProgressLocation());
        sftpConfig.setPartialSuccessLocation(sftpConfigDto.getPartialSuccessLocation());
        sftpConfig.setToBeProcessedLocation(sftpConfigDto.getToBeProcessedLocation());
        return sftpConfig;
    }
}
