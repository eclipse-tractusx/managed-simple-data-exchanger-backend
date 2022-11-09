package com.catenax.sde.submodels.batch.csvhandler;

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
import com.catenax.sde.submodels.batch.entities.Batch;
import com.google.gson.JsonObject;

import lombok.SneakyThrows;


public class BatchCSVHandler implements CSVExtension<Batch> {
	
	
	@Override
    @SneakyThrows
	public Batch parseCSVDataToSubmodel(RowData rowData, String processId, SubmodelFileRequest submodelFileRequest,
			JsonObject asJsonObject) {
		
		 String[] rowDataFields = rowData.content().split(CommonConstants.SEPARATOR, -1);
		 if(rowDataFields.length!= asJsonObject.size()){
			 throw new CsvHandlerUseCaseException(rowData.position(), "This row has the wrong amount of fields");
		 }
		 
	     Batch batch = Batch.builder()
	                .rowNumber(rowData.position())
	                .uuid(rowDataFields[0].trim())
	                .processId(processId)
	                .batchId(rowDataFields[1].trim())
	                .manufacturingDate(rowDataFields[2].trim())
	                .manufacturingCountry(rowDataFields[3].trim().isBlank() ? null : rowDataFields[3])
	                .manufacturerPartId(rowDataFields[4].trim())
	                .customerPartId(rowDataFields[5].trim().isBlank() ? null : rowDataFields[5])
	                .classification(rowDataFields[6].trim())
	                .nameAtManufacturer(rowDataFields[7].trim())
	                .nameAtCustomer(rowDataFields[8].trim().isBlank() ? null : rowDataFields[8])
	                .optionalIdentifierKey(rowDataFields[9].isBlank() ? null : rowDataFields[9])
	                .optionalIdentifierValue(rowDataFields[10].isBlank() ? null : rowDataFields[10])
	                .bpnNumbers(submodelFileRequest.getBpnNumbers())
	                .usagePolicies(submodelFileRequest.getUsagePolicies())
	                .typeOfAccess(submodelFileRequest.getTypeOfAccess())
	                .build();

	        List<String> errorMessages = validateBatchAsset(batch);
	        if (!errorMessages.isEmpty()) {
	            throw new CsvHandlerUseCaseException(rowData.position(), errorMessages.toString());
	        }
	        
	        return batch;
	}
	
	private List<String> validateBatchAsset(Batch batch) {
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();
        Set<ConstraintViolation<Batch>> violations = validator.validate(batch);

        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted().toList();
    }

}
