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
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface
ProcessReportRepository extends JpaRepository<ProcessReportEntity, String> {

    @Modifying
    @Transactional
    @Query("UPDATE ProcessReportEntity " +
            "SET numberOfSucceededItems = numberOfSucceededItems +1 " +
            "WHERE processId = :processId")
    void incrementSucceededItems(String processId);

    @Modifying
    @Transactional
    @Query("UPDATE ProcessReportEntity " +
            "SET numberOfFailedItems = numberOfFailedItems +1 " +
            "WHERE processId = :processId")
    void incrementFailedItems(String processId);

    @Modifying
    @Transactional
    @Query("UPDATE ProcessReportEntity " +
            "SET numberOfFailedItems = numberOfItems - numberOfSucceededItems " +
            "WHERE processId = :processId")
    void calculateFailedItems(String processId);

    @Modifying
    @Transactional
    @Query("UPDATE ProcessReportEntity " +
            "SET endDate = :endDate " +
            "WHERE processId = :processId")
    void setEndDate(String processId, LocalDateTime endDate);

    @Modifying
    @Transactional
    @Query("UPDATE ProcessReportEntity " +
            "SET status = :status " +
            "WHERE processId = :processId")
    void setStatus(ProgressStatusEnum status, String processId);
}
