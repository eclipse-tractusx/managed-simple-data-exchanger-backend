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

import static com.catenax.dft.gateways.file.CsvGateway.SEPARATOR;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.catenax.dft.entities.SubmodelFileRequest;
import com.catenax.dft.entities.csv.RowData;
import com.catenax.dft.entities.usecases.AspectRelationship;
import com.catenax.dft.usecases.common.DftDateValidator;
import com.catenax.dft.usecases.csvhandler.AbstractCsvHandlerUseCase;
import com.catenax.dft.usecases.csvhandler.CsvHandlerOrchestrator;
import com.catenax.dft.usecases.csvhandler.exceptions.CsvHandlerUseCaseException;

import lombok.SneakyThrows;

@Service
public class MapToAspectRelationshipCsvHandlerUseCase
        extends AbstractCsvHandlerUseCase<RowData, AspectRelationship> {

	private SubmodelFileRequest submodelFileRequest;
	
	@Autowired
	private DftDateValidator dftDateValidator;
	
    public MapToAspectRelationshipCsvHandlerUseCase(FetchCatenaXIdCsvHandlerUseCase nextUseCase) {
        super(nextUseCase);
    }

    public void init(SubmodelFileRequest submodelFileRequest) {
		this.submodelFileRequest = submodelFileRequest;
	}
    
    @Override
    @SneakyThrows
    protected AspectRelationship executeUseCase(RowData rowData, String processId) {
        String[] rowDataFields = rowData.content().split(SEPARATOR, -1);
        if (rowDataFields.length != CsvHandlerOrchestrator.getAspectRelationshipColumnSize()) {
            throw new CsvHandlerUseCaseException(rowData.position(), "This row has wrong amount of fields");
        }

        AspectRelationship aspectRelationShip = AspectRelationship.builder()
                .rowNumber(rowData.position())
                .processId(processId)
                .parentUuid(rowDataFields[0].trim())
                .parentPartInstanceId(rowDataFields[1].trim())
                .parentManufacturerPartId(rowDataFields[2].trim())
                .parentOptionalIdentifierKey(rowDataFields[3].trim())
                .parentOptionalIdentifierValue(rowDataFields[4].trim())
                .childUuid(rowDataFields[5].trim())
                .childPartInstanceId(rowDataFields[6].trim())
                .childManufacturerPartId(rowDataFields[7].trim())
                .childOptionalIdentifierKey(rowDataFields[8].trim())
                .childOptionalIdentifierValue(rowDataFields[9].trim())
                .lifecycleContext(rowDataFields[10].trim())
                .quantityNumber(rowDataFields[11].trim())
                .measurementUnitLexicalValue(rowDataFields[12].trim())
                .dataTypeUri(rowDataFields[13].trim())
                .assembledOn(dftDateValidator.getIfValidDateTimeForAssembly(rowDataFields[14].trim(),rowData.position()))
                .build();

        List<String> errorMessages = validateAsset(aspectRelationShip);
        if (!errorMessages.isEmpty()) {
            throw new CsvHandlerUseCaseException(rowData.position(), errorMessages.toString());
        }

        aspectRelationShip.setBpnNumbers(submodelFileRequest.getBpnNumbers());
        aspectRelationShip.setUsagePolicies(submodelFileRequest.getUsagePolicies());
        
        return aspectRelationShip;
    }

    private List<String> validateAsset(AspectRelationship asset) {
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();
        Set<ConstraintViolation<AspectRelationship>> violations = validator.validate(asset);

        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .toList();
    }
}