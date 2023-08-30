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

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.eclipse.tractusx.sde.agent.model.SftpConfigModel;
import org.eclipse.tractusx.sde.agent.repository.SftpReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
public class FtpsService {

    @Autowired
    private SftpConfigModel sftpConfigModel;

    @Autowired
    private FTPClient ftpsClient;

    @Autowired
    private SftpReportRepository sftpReportRepository;

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

}
