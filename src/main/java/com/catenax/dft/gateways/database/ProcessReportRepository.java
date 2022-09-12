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

package com.catenax.dft.gateways.database;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.catenax.dft.entities.database.ProcessReportEntity;

public interface ProcessReportRepository extends JpaRepository<ProcessReportEntity, String> {

    Optional<ProcessReportEntity> findByProcessId(String processId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE process_report " +
            "SET end_date = ?2, " +
            "status = ?3, " +
            "number_of_succeeded_items = " +
            "(SELECT COUNT(a) FROM aspect a WHERE a.process_id = ?1), " +
            "number_of_failed_items = (SELECT COUNT(f) FROM failure_log f WHERE f.process_id = ?1) " +
            "WHERE process_id = ?1", nativeQuery = true)
    void finalizeAspectProgressReport(String processId, LocalDateTime endDate, String status);

    @Modifying
    @Transactional
    @Query(value = "UPDATE process_report " +
            "SET end_date = ?2, " +
            "status = ?3, " +
            "number_of_succeeded_items = " +
            "(SELECT COUNT(c) FROM aspect_relationship c WHERE c.process_id = ?1), " +
            "number_of_failed_items = (SELECT COUNT(f) FROM failure_log f WHERE f.process_id = ?1) " +
            "WHERE process_id = ?1", nativeQuery = true)
    void finalizeChildAspectProgressReport(String processId, LocalDateTime endDate, String status);
    
    @Modifying
    @Transactional
    @Query(value = "UPDATE process_report " +
            "SET end_date = ?2, " +
            "status = ?3, " +
            "number_of_succeeded_items = " +
            "(SELECT COUNT(c) FROM batch c WHERE c.process_id = ?1), " +
            "number_of_failed_items = (SELECT COUNT(f) FROM failure_log f WHERE f.process_id = ?1) " +
            "WHERE process_id = ?1", nativeQuery = true)
    void finalizeBatchProgressReport(String processId, LocalDateTime endDate, String status);


    @Query("SELECT p FROM ProcessReportEntity p ORDER BY p.startDate DESC")
    Page<ProcessReportEntity> findAll(PageRequest pageRequest);
}
