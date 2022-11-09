package com.catenax.sde.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catenax.sde.common.enums.CsvTypeEnum;
import com.catenax.sde.entities.database.AspectEntity;
import com.catenax.sde.exceptions.DftException;
import com.catenax.sde.usecases.aspectrelationship.GetAspectsRelationshipUseCase;
import com.catenax.sde.usecases.aspects.GetAspectsUseCase;
import com.catenax.sde.usecases.csvhandler.delete.DeleteUsecaseHandler;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("delete")
@Validated
public class DeleteController {

    private final GetAspectsRelationshipUseCase aspectsRelationshipUseCase;
    private final DeleteUsecaseHandler deleteUsecaseHandler;
	
    
	public DeleteController(
			GetAspectsRelationshipUseCase aspectsRelationshipUseCase,DeleteUsecaseHandler deleteUsecaseHandler) {
		super();
		this.aspectsRelationshipUseCase = aspectsRelationshipUseCase;
		this.deleteUsecaseHandler=deleteUsecaseHandler;
	}

	@DeleteMapping(value = "/{processId}/{csvType}", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<String> deleteRecordsWithDigitalTwinAndEDC(@PathVariable("processId") String processId,
			@PathVariable("csvType") String csvType) throws JsonProcessingException {
		String delProcessId = UUID.randomUUID().toString();
		
		
		 Runnable runnable = () -> {
	            try {
	            	if (csvType.equalsIgnoreCase(CsvTypeEnum.ASPECT.toString())) {
	        			

	        				deleteUsecaseHandler.deleteAspectDigitalTwinsAndEDC (processId,delProcessId);
	        			
	        		} else if (csvType.equals(CsvTypeEnum.ASPECT_RELATIONSHIP.toString())) {

	        		} else if (csvType.equals(CsvTypeEnum.BATCH.toString())) {

	        		}
	            } catch (JsonProcessingException e) {
	                throw new DftException(e);
	            }
	        };
	        new Thread(runnable).start();

		return ok().body(delProcessId);
	}
}
