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
import com.catenax.dft.enums.CsvTypeEnum;
import com.catenax.dft.enums.ProgressStatusEnum;
import com.catenax.dft.gateways.database.ProcessReportRepository;
import com.catenax.dft.mapper.ProcessReportMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ProcessReportUseCase {
    private final ProcessReportRepository repository;
    private final ProcessReportMapper mapper;

    public ProcessReportUseCase(ProcessReportRepository repository, ProcessReportMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public void startBuildProcessReport(String processId, CsvTypeEnum type, int size, LocalDateTime now) {
        saveProcessReport(ProcessReport.builder()
                .processId(processId)
                .csvType(type)
                .status(ProgressStatusEnum.IN_PROGRESS)
                .numberOfItems(size)
                .startDate(now)
                .build());
    }

    public void finishBuildAspectProgressReport(String processId) {
        repository.finalizeAspectProgressReport(processId, LocalDateTime.now(), ProgressStatusEnum.COMPLETED);
    }

    public void finishBuildChildAspectProgressReport(String processId) {
        repository.finalizeChildAspectProgressReport(processId, LocalDateTime.now(), ProgressStatusEnum.COMPLETED);
    }

    public void unknownProcessReport(String processId, LocalDateTime now) {
        saveProcessReport(ProcessReport.builder()
                .processId(processId)
                .csvType(CsvTypeEnum.UNKNOWN)
                .startDate(now)
                .endDate(now)
                .status(ProgressStatusEnum.FAILED)
                .build());
    }

    private void saveProcessReport(ProcessReport input) {
        ProcessReportEntity entity = mapper.mapFrom(input);
        repository.save(entity);
        log.debug("Process report stored successfully");
    }

    public ProcessReportPageResponse listAllProcessReports(int page, int size) {
        Page<ProcessReportEntity> result = repository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate")));
        List<ProcessReport> processReports = result.get().map(mapper::mapFrom).collect(Collectors.toList());
        return ProcessReportPageResponse.builder()
                .items(processReports)
                .pageSize(result.getSize())
                .pageNumber(result.getNumber())
                .totalItems(result.getTotalElements())
                .build();
    }

    public ProcessReport getProcessReportById(String id) {
        Optional<ProcessReportEntity> result = repository.findById(id);
        return result.map(mapper::mapFrom).orElse(null);
    }
}
