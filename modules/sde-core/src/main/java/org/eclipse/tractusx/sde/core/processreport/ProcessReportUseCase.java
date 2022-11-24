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

package org.eclipse.tractusx.sde.core.processreport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.common.enums.ProgressStatusEnum;
import org.eclipse.tractusx.sde.core.processreport.entity.ProcessReportEntity;
import org.eclipse.tractusx.sde.core.processreport.mapper.ProcessReportMapper;
import org.eclipse.tractusx.sde.core.processreport.model.ProcessReport;
import org.eclipse.tractusx.sde.core.processreport.model.ProcessReportPageResponse;
import org.eclipse.tractusx.sde.core.processreport.repository.ProcessReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.SneakyThrows;

@Component
public class ProcessReportUseCase {

	private static final String UNKNOWN = "UNKNOWN";
	private final ProcessReportRepository repository;
	private final ProcessReportMapper mapper;

	public ProcessReportUseCase(ProcessReportRepository repository, ProcessReportMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@SneakyThrows
	public void startBuildProcessReport(String processId, String type, int size, List<String> bpnNumbers,
			String typeOfAccess, List<UsagePolicies> usagePolicies) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		String usageList = objectMapper.writeValueAsString(usagePolicies);
		saveProcessReport(ProcessReport.builder().processId(processId).csvType(type.toUpperCase())
				.status(ProgressStatusEnum.IN_PROGRESS).numberOfItems(size).startDate(LocalDateTime.now())
				.bpnNumbers(bpnNumbers).typeOfAccess(typeOfAccess).usagePolicies(usageList).build());
	}

	@SneakyThrows
	public void startDeleteProcess(ProcessReport oldProcessReport, String refProcessId, String type, int size,
			String delProcessId) {

		oldProcessReport.setProcessId(delProcessId);
		oldProcessReport.setCsvType(type.toUpperCase());
		oldProcessReport.setStatus(ProgressStatusEnum.IN_PROGRESS);
		oldProcessReport.setNumberOfItems(size);
		oldProcessReport.setStartDate(LocalDateTime.now());
		oldProcessReport.setReferenceProcessId(refProcessId);
		oldProcessReport.setNumberOfDeletedItems(0);
		oldProcessReport.setNumberOfUpdatedItems(0);
		oldProcessReport.setNumberOfSucceededItems(0);
		oldProcessReport.setNumberOfFailedItems(0);

		saveProcessReport(oldProcessReport);
	}

	public void unknownProcessReport(String processId) {
		LocalDateTime now = LocalDateTime.now();
		saveProcessReport(ProcessReport.builder().processId(processId).csvType(UNKNOWN).startDate(now).endDate(now)
				.status(ProgressStatusEnum.FAILED).build());
	}

	public void saveProcessReport(ProcessReport input) {
		ProcessReportEntity entity = mapper.mapFrom(input);
		repository.save(entity);
	}

	public ProcessReportPageResponse listAllProcessReports(int page, int size) {
		Page<ProcessReportEntity> result = repository
				.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate")));
		List<ProcessReport> processReports = result.get().map(mapper::mapFrom).toList();
		return ProcessReportPageResponse.builder().items(processReports).pageSize(result.getSize())
				.pageNumber(result.getNumber()).totalItems(result.getTotalElements()).build();
	}

	public ProcessReport getProcessReportById(String id) {
		Optional<ProcessReportEntity> result = repository.findByProcessId(id);
		return result.map(mapper::mapFrom).orElse(null);
	}

	public void finishBuildProgressReport(String processId, int successCount, int failedCount, int updatedcount) {
		repository.finalizeProgressReport(processId, LocalDateTime.now(), ProgressStatusEnum.COMPLETED.toString(),
				successCount, failedCount,updatedcount);

	}

	public void finishBuildDeleteProgressReport(String processId, int deletedCount, int failedCount) {
		repository.finalizeProgressDeleteReport(processId, LocalDateTime.now(), ProgressStatusEnum.COMPLETED.toString(),
				deletedCount, failedCount);

	}
}
