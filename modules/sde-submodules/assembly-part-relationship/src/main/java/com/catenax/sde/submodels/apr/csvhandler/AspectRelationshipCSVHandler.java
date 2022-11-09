package com.catenax.sde.submodels.apr.csvhandler;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import com.catenax.sde.common.constants.CommonConstants;
import com.catenax.sde.common.entities.SubmodelFileRequest;
import com.catenax.sde.common.entities.csv.RowData;
import com.catenax.sde.common.exception.CsvHandlerUseCaseException;
import com.catenax.sde.common.extensions.CSVExtension;
import com.catenax.sde.submodels.apr.entities.AspectRelationship;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;

public class AspectRelationshipCSVHandler implements CSVExtension<AspectRelationship> {

	@Override
    @SneakyThrows
	public AspectRelationship parseCSVDataToSubmodel(RowData rowData, String processId,
			SubmodelFileRequest submodelFileRequest, JsonObject asJsonObject) {
		
		String[] rowDataFields = rowData.content().split(CommonConstants.SEPARATOR, -1);
		 if(rowDataFields.length!= asJsonObject.size()){
			 throw new CsvHandlerUseCaseException(rowData.position(), "This row has the wrong amount of fields");
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
	                .assembledOn(rowDataFields[14].trim())
	                .bpnNumbers(submodelFileRequest.getBpnNumbers())
	                .usagePolicies(submodelFileRequest.getUsagePolicies())
	                .typeOfAccess(submodelFileRequest.getTypeOfAccess())
	                .build();
		 
		 List<String> errorMessages = validateAsset(aspectRelationShip);
	        if (!errorMessages.isEmpty()) {
	            throw new CsvHandlerUseCaseException(rowData.position(), errorMessages.toString());
	        }
		
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
