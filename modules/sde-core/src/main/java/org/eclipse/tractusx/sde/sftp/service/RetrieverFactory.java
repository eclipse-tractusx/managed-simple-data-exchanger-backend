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

import com.fasterxml.jackson.databind.JsonNode;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.eclipse.tractusx.sde.sftp.RetrieverI;

import java.io.IOException;

public interface RetrieverFactory {
    /***
     * Successful creation of the retriever means the RetrieverConfiguration was correct
     * and the retriever managed to log in to the remote resource
     * @return retriever
     */
    RetrieverI create() throws IOException;
    void saveConfig(JsonNode config);
}
