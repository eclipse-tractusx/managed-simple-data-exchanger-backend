/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.catenax.dft.entities.database;

import com.catenax.dft.enums.ProgressStatusEnum;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Table(name = "process_report")
@Entity
@Data
@Cacheable(value = false)
public class ProcessReportEntity implements Serializable {
    @Id
    @Column(name = "process_id")
    private String processId;
    @Column(name = "csv_type")
    private String csvType;
    @Column(name = "number_of_items")
    private int numberOfItems;
    @Column(name = "number_of_failed_items")
    private int numberOfFailedItems;
    @Column(name = "number_of_succeeded_items")
    private int numberOfSucceededItems;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProgressStatusEnum status;
    @Column(name = "start_date")
    private LocalDateTime startDate;
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Convert(converter = ListToStringConverter.class)
    @Column(name = "bpn_numbers")
    private List<String> bpnNumbers;

    @Column(name = "type_of_access")
    private String typeOfAccess;

    @Column(name = "usage_policy", columnDefinition = "jsonb")
    private String usagePolicies;
}
