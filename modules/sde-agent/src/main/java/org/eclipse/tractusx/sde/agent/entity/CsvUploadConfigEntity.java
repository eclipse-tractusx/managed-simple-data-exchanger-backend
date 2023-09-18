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

package org.eclipse.tractusx.sde.agent.entity;

import jakarta.persistence.*;
import lombok.Data;

@Table(name = "ftps_config")
@Entity
@Data
public class CsvUploadConfigEntity {

    public final static String SFTP_CLIENT_CONFIG_ID = "755FCA09-884B-4A05-91C9-EB2D3E7A45FF";
    public final static String METADATA_CONFIG_ID = "878ef54e-ec3d-4e9b-b775-cb8e83151570";
    public final static String SCHEDULER_CONFIG_ID = "e08cf6f2-1ff7-4c8b-9c4b-5572eeb3fc3c";
    public final static String NOTIFICATION_CONFIG_ID = "5973a8fe-19d1-4633-a6c8-993ee9c4705c";
    public final static String JOB_MAINTENANCE_CONFIG_ID = "b7fae548-083f-4947-aa54-a11c9ee918ec";
    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "type")
    private String type;

    @Column(name = "content")
    private String content;
}
