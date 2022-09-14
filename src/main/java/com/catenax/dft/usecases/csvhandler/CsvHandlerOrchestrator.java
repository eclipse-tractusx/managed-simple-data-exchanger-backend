/********************************************************************************
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

package com.catenax.dft.usecases.csvhandler;


import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.catenax.dft.entities.SubmodelFileRequest;
import com.catenax.dft.entities.csv.CsvContent;
import com.catenax.dft.enums.CsvTypeEnum;
import com.catenax.dft.exceptions.DftException;
import com.catenax.dft.usecases.csvhandler.aspectrelationship.MapToAspectRelationshipCsvHandlerUseCase;
import com.catenax.dft.usecases.csvhandler.aspects.MapToAspectCsvHandlerUseCase;
import com.catenax.dft.usecases.csvhandler.batchs.MapToBatchCsvHandlerUseCase;
import com.catenax.dft.usecases.processreport.ProcessReportUseCase;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CsvHandlerOrchestrator {
	
	private static final String MANUFACTURE_PART_ID="manufacturer_part_id";
	private static final String OPTIONAL_IDENTIFIER_KEY="optional_identifier_key";
	private static final String OPTIONAL_IDENTIFIER_VALUE="optional_identifier_value";

    private static final List<String> ASPECT_COLUMNS = Stream.of(
            "UUID",
            "part_instance_id",
            "manufacturing_date",
            "manufacturing_country",
            MANUFACTURE_PART_ID,
            "customer_part_id",
            "classification",
            "name_at_manufacturer",
            "name_at_customer",
            OPTIONAL_IDENTIFIER_KEY,
            OPTIONAL_IDENTIFIER_VALUE)
            .toList();
    private static final List<String> BATCH_COLUMNS = Stream.of(
            "UUID",
            "batch_id",
            "manufacturing_date",
            "manufacturing_country",
            MANUFACTURE_PART_ID,
            "customer_part_id",
            "classification",
            "name_at_manufacturer",
            "name_at_customer",
            OPTIONAL_IDENTIFIER_KEY,
            OPTIONAL_IDENTIFIER_VALUE)
            .toList();
    private static final List<String> ASPECT_RELATIONSHIP_COLUMNS = Stream.of(
            "parent_UUID",
            "parent_part_instance_id",
            "parent_manufacturer_part_id",
            "parent_optional_identifier_key",
            "parent_optional_identifier_value",
            "UUID",
            "part_instance_id",
            MANUFACTURE_PART_ID,
            OPTIONAL_IDENTIFIER_KEY,
            OPTIONAL_IDENTIFIER_VALUE,
            "lifecycle_context",
            "quantity_number",
            "measurement_unit_lexical_value",
            "datatype_URI",
            "assembled_on")
            .toList();

    private final MapToAspectCsvHandlerUseCase aspectStarterUseCase;
    private final MapToBatchCsvHandlerUseCase batchStarterUseCase;
    private final MapToAspectRelationshipCsvHandlerUseCase aspectRelationshipStarterUseCase;
    private final ProcessReportUseCase processReportUseCase;

    public CsvHandlerOrchestrator(MapToAspectCsvHandlerUseCase aspectStarterUseCase,
    							  MapToBatchCsvHandlerUseCase batchStarterUseCase,
                                  MapToAspectRelationshipCsvHandlerUseCase aspectRelationshipStarterUseCase,
                                  ProcessReportUseCase processReportUseCase) {
        this.aspectStarterUseCase = aspectStarterUseCase;
        this.batchStarterUseCase = batchStarterUseCase;
        this.aspectRelationshipStarterUseCase = aspectRelationshipStarterUseCase;
        this.processReportUseCase = processReportUseCase;
    }

    public static int getAspectColumnSize(){
        return ASPECT_COLUMNS.size();
    }
    
    public static int getBatchColumnSize(){
        return BATCH_COLUMNS.size();
    }

    public static int getAspectRelationshipColumnSize(){
        return ASPECT_RELATIONSHIP_COLUMNS.size();
    }

    @SneakyThrows
    public void execute(CsvContent csvContent, String processId, SubmodelFileRequest submodelFileRequest) {
        if (ASPECT_COLUMNS.equals(csvContent.getColumns())) {
            processReportUseCase.startBuildProcessReport(processId, CsvTypeEnum.ASPECT, csvContent.getRows().size(),
                    submodelFileRequest.getBpnNumbers(),submodelFileRequest.getTypeOfAccess(), submodelFileRequest.getUsagePolicies());
            log.debug("I'm an ASPECT file. Unpacked and ready to be processed.");
            
            csvContent.getRows().parallelStream().forEach(input ->{
            	aspectStarterUseCase.init(submodelFileRequest);
            	aspectStarterUseCase.run(input, processId);
            	});
            
            processReportUseCase.finishBuildAspectProgressReport(processId);
        }else if (BATCH_COLUMNS.equals(csvContent.getColumns())) {
            processReportUseCase.startBuildProcessReport(processId, CsvTypeEnum.BATCH, csvContent.getRows().size(),
                    submodelFileRequest.getBpnNumbers(),submodelFileRequest.getTypeOfAccess(), submodelFileRequest.getUsagePolicies());
            log.debug("I'm an BATCH file. Unpacked and ready to be processed.");
            
            csvContent.getRows().parallelStream().forEach(input -> {
            	batchStarterUseCase.init(submodelFileRequest);
            	batchStarterUseCase.run(input, processId);
            });
            
            processReportUseCase.finishBuildBatchProgressReport(processId);
        } else if (ASPECT_RELATIONSHIP_COLUMNS.equals(csvContent.getColumns())) {
            processReportUseCase.startBuildProcessReport(processId, CsvTypeEnum.ASPECT_RELATIONSHIP, csvContent.getRows().size(),
                    submodelFileRequest.getBpnNumbers(),submodelFileRequest.getTypeOfAccess(), submodelFileRequest.getUsagePolicies());
            log.debug("I'm an ASPECT RELATIONSHIP file. Unpacked and ready to be processed.");
            
            csvContent.getRows().parallelStream().forEach(input -> {
            	aspectRelationshipStarterUseCase.init(submodelFileRequest);
            	aspectRelationshipStarterUseCase.run(input, processId);
            });
            
            processReportUseCase.finishBuildChildAspectProgressReport(processId);
        } else {
            processReportUseCase.unknownProcessReport(processId);
            throw new DftException("Unrecognized CSV content. I don't know what to do with you");
        }
    }
}