/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the CatenaX (ng) GitHub Organisation
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

package com.catenax.sde.usecases.processreport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.catenax.sde.edc.entities.UsagePolicy;
import com.catenax.sde.entities.database.ProcessReportEntity;
import com.catenax.sde.entities.usecases.ProcessReport;
import com.catenax.sde.entities.usecases.ProcessReportPageResponse;
import com.catenax.sde.enums.CsvTypeEnum;
import com.catenax.sde.enums.ProgressStatusEnum;
import com.catenax.sde.gateways.database.ProcessReportRepository;
import com.catenax.sde.mapper.ProcessReportMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProcessReportUseCase {
    private final ProcessReportRepository repository;
    private final ProcessReportMapper mapper;

    public ProcessReportUseCase(ProcessReportRepository repository, ProcessReportMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public void startBuildProcessReport(String processId, CsvTypeEnum type, int size, List<String> bpnNumbers,
                                        String typeOfAccess, List<UsagePolicy> usagePolicies) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        String usageList = objectMapper.writeValueAsString(usagePolicies);
        saveProcessReport(ProcessReport.builder()
                .processId(processId)
                .csvType(type)
                .status(ProgressStatusEnum.IN_PROGRESS)
                .numberOfItems(size)
                .startDate(LocalDateTime.now())
                .bpnNumbers(bpnNumbers)
                .typeOfAccess(typeOfAccess)
                .usagePolicies(usageList)
                .build());
    }
    
    public void startDeleteProcess(String processId, CsvTypeEnum type, int size, String referenceProcessId,
           int numberOfDeletedItems) throws JsonProcessingException {
				saveProcessReport(ProcessReport.builder()
				.processId(processId)
				.csvType(type)
				.status(ProgressStatusEnum.IN_PROGRESS)
				.numberOfItems(size)
				.startDate(LocalDateTime.now())
				.referenceProcessId(referenceProcessId)
				.numberOfDeletedItems(numberOfDeletedItems)
				.build());
}
    

    public void finishBuildAspectProgressReport(String processId) {
        repository.finalizeAspectProgressReport(processId, LocalDateTime.now(), ProgressStatusEnum.COMPLETED.toString());
    }
    public void finishBuildBatchProgressReport(String processId) {
        repository.finalizeBatchProgressReport(processId, LocalDateTime.now(), ProgressStatusEnum.COMPLETED.toString());
    }

    public void finishBuildChildAspectProgressReport(String processId) {
        repository.finalizeChildAspectProgressReport(processId, LocalDateTime.now(), ProgressStatusEnum.COMPLETED.toString());
    }
    
    public void finalizeNoOfDeletedInProgressReport(String processId, int numberOfDeletedItems,String refProcessId) {
        repository.finalizeNoOfDeletedInProgressReport(processId, LocalDateTime.now(), ProgressStatusEnum.COMPLETED.toString(),numberOfDeletedItems,refProcessId);
    }


    public void unknownProcessReport(String processId) {
        LocalDateTime now = LocalDateTime.now();
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
    }

    public ProcessReportPageResponse listAllProcessReports(int page, int size) {
        Page<ProcessReportEntity> result = repository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate")));
        List<ProcessReport> processReports = result.get().map(mapper::mapFrom).toList();
        return ProcessReportPageResponse.builder()
                .items(processReports)
                .pageSize(result.getSize())
                .pageNumber(result.getNumber())
                .totalItems(result.getTotalElements())
                .build();
    }

    public ProcessReport getProcessReportById(String id) {
        Optional<ProcessReportEntity> result = repository.findByProcessId(id);
        return result.map(mapper::mapFrom).orElse(null);
    }
}
