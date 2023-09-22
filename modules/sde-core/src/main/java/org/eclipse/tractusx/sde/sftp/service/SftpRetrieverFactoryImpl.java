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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.tractusx.sde.agent.entity.CsvUploadConfigEntity;
import org.eclipse.tractusx.sde.agent.model.SftpConfigModel;
import org.eclipse.tractusx.sde.agent.repository.CsvUploadConfigRepository;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.sftp.RetrieverI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.OptionalInt;

@Service
@Profile("SSH")
@RequiredArgsConstructor
public class SftpRetrieverFactoryImpl implements RetrieverFactory {

    @Value("${sftp.host}")
    private String host;
    @Value("${sftp.port:22}")
    private int port;
    @Value("${sftp.username}")
    private String username;
    @Value("${sftp.password}")
    private String password;
    @Value("${sftp.location.tobeprocessed}")
    private String toBeProcessed;
    @Value("${sftp.location.inprogress}")
    private String inProgress;
    @Value("${sftp.location.success}")
    private String success;
    @Value("${sftp.location.partialsucess}")
    private String partialSuccess;
    @Value("${sftp.location.failed}")
    private String failed;

    private final CsvUploadConfigRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CsvHandlerService csvHandlerService;


    public RetrieverI create(OptionalInt port) throws IOException {
        try {
            var configEntityOptional = repository.findById(CsvUploadConfigEntity.SFTP_CLIENT_CONFIG_ID);
            if (configEntityOptional.isPresent()) {
                SftpConfigModel configModel = objectMapper.readValue(configEntityOptional.get().getContent(), SftpConfigModel.class);
                return new SftpRetriever(
                        csvHandlerService,
                        configModel.getHost(),
                        port.orElse(configModel.getPort()),
                        configModel.getUsername(),
                        configModel.getPassword(),
                        configModel.getAccessKey(),
                        configModel.getToBeProcessedLocation(),
                        configModel.getInProgressLocation(),
                        configModel.getSuccessLocation(),
                        configModel.getPartialSuccessLocation(),
                        configModel.getFailedLocation()
                );
            } else {
                return new SftpRetriever(
                        csvHandlerService,
                        host,
                        port.orElse(this.port),
                        username,
                        password,
                        null,
                        toBeProcessed,
                        inProgress,
                        success,
                        partialSuccess,
                        failed
                );
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public RetrieverI create() throws IOException {
        return create(OptionalInt.empty());
    }

    @Override
    @Transactional
    public String saveConfig(JsonNode configuration) {
        var configEntityOptional = repository.findById(CsvUploadConfigEntity.SFTP_CLIENT_CONFIG_ID);
        CsvUploadConfigEntity configEntity;
        if (configEntityOptional.isPresent()) {
            configEntity = configEntityOptional.get();
            configEntity.setContent(configuration.toString());
        } else {
            configEntity = new CsvUploadConfigEntity();
            configEntity.setUuid(CsvUploadConfigEntity.SFTP_CLIENT_CONFIG_ID);
            configEntity.setContent(configuration.toString());
            configEntity.setType(ConfigType.CLIENT.toString());
        }
        repository.save(configEntity);
        return configEntity.getUuid();
    }

    @SneakyThrows
    public SftpConfigModel getConfig() {
        var configEntityOptional = repository.findById(CsvUploadConfigEntity.SFTP_CLIENT_CONFIG_ID);
        SftpConfigModel configModel;
        if (configEntityOptional.isPresent()) {
            try {
                configModel = objectMapper.readValue(configEntityOptional.get().getContent(), SftpConfigModel.class);
            } catch (JsonProcessingException e) {
                throw new ServiceException("Error while getting the SFTP config");
            }
            return configModel;
        } else {
            SftpConfigModel sftpConfigModel =  SftpConfigModel.builder()
                    .host(host)
                    .port(port)
                    .failedLocation(failed)
                    .username(username)
                    .password(password)
                    .toBeProcessedLocation(toBeProcessed)
                    .inProgressLocation(inProgress)
                    .partialSuccessLocation(partialSuccess)
                    .successLocation(success)
                    .build();

           return sftpConfigModel;
        }

    }
}
