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


package org.eclipse.tractusx.sde.agent.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.eclipse.tractusx.sde.agent.enums.SftpReportStatusEnum;

import java.io.Serializable;
import java.time.LocalDateTime;

@Table(name = "sftp_scheduler_report")
@Entity
@Data
public class SftpSchedulerReport implements Serializable {
    @Id
    @Column(name = "process_id")
    private String processId;
    @Column(name = "scheduler_id")
    private String schedulerId;
    @Column(name = "file_name")
    private String fileName;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SftpReportStatusEnum status;
    @Column(name = "start_date")
    private LocalDateTime startDate;
    @Column(name = "end_date")
    private LocalDateTime endDate;
}
