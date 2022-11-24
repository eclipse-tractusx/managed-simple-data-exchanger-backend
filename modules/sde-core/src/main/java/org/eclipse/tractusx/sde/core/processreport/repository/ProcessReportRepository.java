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

package org.eclipse.tractusx.sde.core.processreport.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.eclipse.tractusx.sde.core.processreport.entity.ProcessReportEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ProcessReportRepository extends JpaRepository<ProcessReportEntity, String> {

    Optional<ProcessReportEntity> findByProcessId(String processId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE process_report " +
            "SET end_date = ?2, " +
            "status = ?3, " +
            "number_of_deleted_items =?4, " +
            "number_of_failed_items = ?5 " +
            "WHERE process_id = ?1", nativeQuery = true)
	void finalizeProgressDeleteReport(String processId, LocalDateTime endDate, String status, int deletedCount,
			int noOfFailed);
    
    
    @Modifying
    @Transactional
    @Query(value = "UPDATE process_report " +
            "SET end_date = ?2, " +
            "status = ?3, " +
            "number_of_succeeded_items =?4, " +
            "number_of_failed_items = ?5, " +
            "number_of_updated_items = ?6 " +            
            "WHERE process_id = ?1", nativeQuery = true)
	void finalizeProgressReport(String processId, LocalDateTime endDate, String status, int successCount,
			int noOfFailed,long noOfUpdated);

    
    @Query("SELECT p FROM ProcessReportEntity p ORDER BY p.startDate DESC")
    Page<ProcessReportEntity> findAll(PageRequest pageRequest);
}
