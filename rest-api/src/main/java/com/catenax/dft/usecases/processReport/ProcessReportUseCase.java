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

package com.catenax.dft.usecases.processReport;

import com.catenax.dft.entities.database.ProcessReportEntity;
import com.catenax.dft.entities.usecases.ProcessReport;
import com.catenax.dft.entities.usecases.ProcessReportPageResponse;
import com.catenax.dft.enums.ProgressStatusEnum;
import com.catenax.dft.enums.CsvTypeEnum;
import com.catenax.dft.gateways.database.ProcessReportRepository;
import com.catenax.dft.mapper.ProcessReportMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ProcessReportUseCase {
    private final ProcessReportRepository repository;
    private ProcessReport processReport;
    private final ProcessReportMapper mapper;

    public ProcessReport getReportsById(String id){
        Optional<ProcessReportEntity> result = repository.findById(id);
        if( result.isPresent()){
            return mapper.mapFrom(result.get());
        }
        return null;

    }


    public ProcessReportUseCase(ProcessReportRepository repository, ProcessReportMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public void startBuildHistoricFile(String processId, CsvTypeEnum aspect, int size, LocalDateTime now) {
        processReport = new ProcessReport();
        processReport.setProcessId(processId);
        processReport.setCsvType(aspect);
        processReport.setStatus(ProgressStatusEnum.IN_PROGRESS);
        processReport.setNumberOfItems(size);
        processReport.setStartDate(now);
        saveHistoric(processReport);
    }

    public void inProgressBuildHistoricFile(int numberOfSucceeded, int numberOfFailures) {
        processReport.setNumberOfSucceededItems(numberOfSucceeded);
        processReport.setNumberOfFailedItems(numberOfFailures);
    }

    public void finishBuildHistoricFile(String processId) {
        repository.setEndDate(processId, LocalDateTime.now());
        repository.setStatus(ProgressStatusEnum.COMPLETED, processId);
        repository.calculateFailedItems(processId);
    }

    public void unknownFileToHistoricFile(String processId, LocalDateTime now) {
        processReport = new ProcessReport();
        processReport.setProcessId(processId);
        processReport.setCsvType(CsvTypeEnum.UNKNOWN);
        processReport.setStartDate(now);
        processReport.setEndDate(now);
        processReport.setStatus(ProgressStatusEnum.FAILED);
        saveHistoric(processReport);
    }

    private void saveHistoric(ProcessReport input) {
        ProcessReportEntity entity = mapper.mapFrom(input);
        repository.save(entity);
        log.debug("Historic store successfully");
    }

    public ProcessReportPageResponse listAllHistoric(int page, int size) {
        Page<ProcessReportEntity> result = repository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate")));
        List<ProcessReport> processReports = result.get().map(mapper::mapFrom).collect(Collectors.toList());
        return ProcessReportPageResponse.builder()
                .items(processReports)
                .pageSize(result.getSize())
                .pageNumber(result.getNumber())
                .totalItems(result.getTotalElements())
                .build();
    }

    public void addSuccess(String pid) {
        repository.incrementSucceededItems(pid);
    }

    public void addFailure(String processId) {
        repository.incrementFailedItems(processId);
    }
}
