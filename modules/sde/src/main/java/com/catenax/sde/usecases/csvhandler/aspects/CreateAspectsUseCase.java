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

package com.catenax.sde.usecases.csvhandler.aspects;

import java.util.List;

import com.catenax.sde.entities.SubmodelJsonRequest;
import com.catenax.sde.entities.aspect.AspectRequest;
import com.catenax.sde.enums.CsvTypeEnum;
import com.catenax.sde.usecases.processreport.ProcessReportUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

@Service
public class CreateAspectsUseCase {
    private final MapFromAspectRequestUseCase useCase;
    private final ProcessReportUseCase processReportUseCase;

    public CreateAspectsUseCase(MapFromAspectRequestUseCase useCase, ProcessReportUseCase processReportUseCase) {
        this.useCase = useCase;
        this.processReportUseCase = processReportUseCase;
    }

    public void createAspects(SubmodelJsonRequest<AspectRequest> aspectInputs, String processId) throws JsonProcessingException {
        List<AspectRequest> rowData = aspectInputs.getRowData();
		processReportUseCase.startBuildProcessReport(processId, CsvTypeEnum.ASPECT, rowData.size(),
                aspectInputs.getBpnNumbers(), aspectInputs.getTypeOfAccess(), aspectInputs.getUsagePolicies());
      
		for(int i=0; i<rowData.size();i++){
            AspectRequest aspect = rowData.get(i);
            aspect.setRowNumber(i);
            aspect.setProcessId(processId);
            aspect.setBpnNumbers(aspectInputs.getBpnNumbers());
            aspect.setUsagePolicies(aspectInputs.getUsagePolicies());
            useCase.run(aspect, processId);
        }

        processReportUseCase.finishBuildAspectProgressReport(processId);
    }
}