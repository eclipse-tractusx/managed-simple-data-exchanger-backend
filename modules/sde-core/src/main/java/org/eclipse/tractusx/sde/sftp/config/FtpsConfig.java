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

package org.eclipse.tractusx.sde.sftp.config;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.eclipse.tractusx.sde.agent.entity.SftpConfig;
import org.eclipse.tractusx.sde.agent.mapper.SftpConfigMapper;
import org.eclipse.tractusx.sde.agent.model.SftpConfigModel;
import org.eclipse.tractusx.sde.agent.repository.SftpConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Configuration
public class FtpsConfig {
    @Value("${ftps.url}")
    private String serverUrl;
    @Value("${ftps.username}")
    private String username;
    @Value("${ftps.password}")
    private String password;
    @Value("${ftps.location.tobeprocessed}")
    private String toBeProcessed;
    @Value("${ftps.location.inprogress}")
    private String inProgress;
    @Value("${ftps.location.success}")
    private String success;
    @Value("${ftps.location.partialsucess}")
    private String partialSuccess;
    @Value("${ftps.location.failed}")
    private String failed;

    @Autowired
    private SftpConfigRepository sftpConfigRepository;

    @Autowired
    private SftpConfigMapper sftpConfigMapper;

    @Bean
    public FTPClient getClient() throws IOException {
        FTPClient ftps = new FTPClient();

        // below line is only for debugging purpose. can comment this line if not needed
        ftps.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

        ftps.connect(serverUrl);
        int reply = ftps.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftps.disconnect();
            throw new IOException("Exception in connecting to FTP Server");
        }
        return ftps;
    }

    @Bean
    public SftpConfigModel getFtpsConfig() {
        List<SftpConfig> configList = sftpConfigRepository.findAll();
        SftpConfigModel sftpConfigModel;

        if(configList != null && !configList.isEmpty()) {
            sftpConfigModel = SftpConfigModel.builder()
                    .url(configList.get(0).getUrl())
                    .username(configList.get(0).getUsername())
                    .password(configList.get(0).getPassword())
                    .toBeProcessedLocation(configList.get(0).getToBeProcessedLocation())
                    .inProgressLocation(configList.get(0).getInProgressLocation())
                    .successLocation(configList.get(0).getSuccessLocation())
                    .partialSuccessLocation(configList.get(0).getPartialSuccessLocation())
                    .failedLocation(configList.get(0).getFailedLocation())
                    .build();
        } else {
            sftpConfigModel = SftpConfigModel.builder()
                    .url(serverUrl)
                    .username(username)
                    .password(password)
                    .toBeProcessedLocation(toBeProcessed)
                    .inProgressLocation(inProgress)
                    .successLocation(success)
                    .partialSuccessLocation(partialSuccess)
                    .failedLocation(failed)
                    .build();
            // connect and store that in the DB
            SftpConfig sftpConfigEntity = sftpConfigMapper.mapFrom(sftpConfigModel);
            sftpConfigRepository.save(sftpConfigEntity);
        }
         return sftpConfigModel;
    }
}
