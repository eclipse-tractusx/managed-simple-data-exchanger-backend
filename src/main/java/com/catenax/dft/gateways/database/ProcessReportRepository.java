/*
 * Copyright 2022 CatenaX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.catenax.dft.gateways.database;

import com.catenax.dft.entities.database.ProcessReportEntity;
import com.catenax.dft.enums.ProgressStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface
ProcessReportRepository extends CrudRepository<ProcessReportEntity, String> {

    Optional<ProcessReportEntity> findByProcessId(String processId);

    @Modifying
    @Transactional
    @Query("UPDATE ProcessReportEntity " +
            "SET endDate = :endDate, " +
            "status = :status, " +
            "numberOfSucceededItems = " +
            "(SELECT COUNT(a) FROM AspectEntity a WHERE a.processId = :processId), " +
            "numberOfFailedItems = (SELECT COUNT(f) FROM FailureLogEntity f WHERE f.processId = :processId) " +
            "WHERE processId = :processId")
    void finalizeAspectProgressReport(String processId, LocalDateTime endDate, ProgressStatusEnum status);

    @Modifying
    @Transactional
    @Query("UPDATE ProcessReportEntity " +
            "SET endDate = :endDate, " +
            "status = :status, " +
            "numberOfSucceededItems = " +
            "(SELECT COUNT(c) FROM AspectRelationshipEntity c WHERE c.processId = :processId), " +
            "numberOfFailedItems = (SELECT COUNT(f) FROM FailureLogEntity f WHERE f.processId = :processId) " +
            "WHERE processId = :processId")
    void finalizeChildAspectProgressReport(String processId, LocalDateTime endDate, ProgressStatusEnum status);


    @Query("SELECT p FROM ProcessReportEntity p ORDER BY p.startDate DESC")
    Page<ProcessReportEntity> findAll(PageRequest startDate);
}
