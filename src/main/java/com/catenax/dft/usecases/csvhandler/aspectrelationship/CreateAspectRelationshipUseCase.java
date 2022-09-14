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

package com.catenax.dft.usecases.csvhandler.aspectrelationship;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import com.catenax.dft.entities.SubmodelJsonRequest;
import com.catenax.dft.entities.aspectrelationship.AspectRelationshipRequest;
import com.catenax.dft.enums.CsvTypeEnum;
import com.catenax.dft.usecases.processreport.ProcessReportUseCase;

@Service
public class CreateAspectRelationshipUseCase {
	private final MapFromAspectRelationshipRequestUseCase useCase;
	private final ProcessReportUseCase processReportUseCase;

	public CreateAspectRelationshipUseCase(MapFromAspectRelationshipRequestUseCase useCase,
			ProcessReportUseCase processReportUseCase) {
		this.useCase = useCase;
		this.processReportUseCase = processReportUseCase;
	}

	public void createAspects(SubmodelJsonRequest<AspectRelationshipRequest> aspectInputs, String processId) throws JsonProcessingException {
		List<AspectRelationshipRequest> rowData = aspectInputs.getRowData();
		processReportUseCase.startBuildProcessReport(processId, CsvTypeEnum.ASPECT_RELATIONSHIP, rowData.size(),
				 aspectInputs.getBpnNumbers(),aspectInputs.getTypeOfAccess(), aspectInputs.getUsagePolicies());

		for (int i = 0; i < rowData.size(); i++) {
			AspectRelationshipRequest aspect = rowData.get(i);
			aspect.setRowNumber(i);
			aspect.setProcessId(processId);
			aspect.setBpnNumbers(aspectInputs.getBpnNumbers());
			aspect.setUsagePolicies(aspectInputs.getUsagePolicies());
			useCase.run(aspect, processId);
		}

		processReportUseCase.finishBuildChildAspectProgressReport(processId);
	}

}
