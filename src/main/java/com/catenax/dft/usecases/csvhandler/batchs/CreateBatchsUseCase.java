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
 *
 */

package com.catenax.dft.usecases.csvhandler.batchs;

import java.util.List;

import org.springframework.stereotype.Service;

import com.catenax.dft.entities.SubmodelJsonRequest;
import com.catenax.dft.entities.aspect.AspectRequest;
import com.catenax.dft.entities.batch.BatchRequest;
import com.catenax.dft.enums.CsvTypeEnum;
import com.catenax.dft.usecases.processreport.ProcessReportUseCase;

@Service
public class CreateBatchsUseCase {
    private final MapFromBatchRequestUseCase useCase;
    private final ProcessReportUseCase processReportUseCase;

    public CreateBatchsUseCase(MapFromBatchRequestUseCase useCase, ProcessReportUseCase processReportUseCase) {
        this.useCase = useCase;
        this.processReportUseCase = processReportUseCase;
    }

    public void createBatchs(SubmodelJsonRequest<BatchRequest> batchInputs, String processId){
        List<BatchRequest> rowData = batchInputs.getRowData();
		processReportUseCase.startBuildProcessReport(processId, CsvTypeEnum.BATCH, rowData.size(), batchInputs.getBpnNumbers(), batchInputs.getTypeOfAccess());
      
		for(int i=0; i<rowData.size();i++){
            BatchRequest batch = rowData.get(i);
            batch.setRowNumber(i);
            batch.setProcessId(processId);
            batch.setBpnNumbers(batchInputs.getBpnNumbers());
            useCase.run(batch, processId);
        }

        processReportUseCase.finishBuildBatchProgressReport(processId);
    }
}