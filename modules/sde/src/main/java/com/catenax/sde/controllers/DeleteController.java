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
import com.catenax.sde.usecases.csvhandler.delete.aspectrelationship.DeleteAspectRelationshipUseCaseHandler;
import com.catenax.sde.usecases.csvhandler.delete.batch.DeleteBatchUseCaseHandler;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("delete")
@Validated
public class DeleteController {

    private final DeleteUsecaseHandler deleteUsecaseHandler;
    private final DeleteAspectRelationshipUseCaseHandler deleteAspectRelationshipUseCaseHandler;
    private final DeleteBatchUseCaseHandler deleteBatchUseCaseHandler;
	
    
	public DeleteController(
		DeleteUsecaseHandler deleteUsecaseHandler,DeleteAspectRelationshipUseCaseHandler deleteAspectRelationshipUseCaseHandler,DeleteBatchUseCaseHandler deleteBatchUseCaseHandler) {
		super();
		this.deleteUsecaseHandler=deleteUsecaseHandler;
		this.deleteAspectRelationshipUseCaseHandler=deleteAspectRelationshipUseCaseHandler;
		this.deleteBatchUseCaseHandler= deleteBatchUseCaseHandler;
	}

	@DeleteMapping(value = "/{processId}/{csvType}", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<String> deleteRecordsWithDigitalTwinAndEDC(@PathVariable("processId") String processId,
			@PathVariable("csvType") String csvType) {
		String delProcessId = UUID.randomUUID().toString();

		Runnable runnable = () -> {
			try {
				if (csvType.equalsIgnoreCase(CsvTypeEnum.ASPECT.toString())) {

					deleteUsecaseHandler.deleteAspectDigitalTwinsAndEDC(processId, delProcessId);

				} else if (csvType.equals(CsvTypeEnum.ASPECT_RELATIONSHIP.toString())) {
					deleteAspectRelationshipUseCaseHandler.deleteAspectRelationshipDigitalTwinsAndEDC(processId,
							delProcessId);

				} else if (csvType.equals(CsvTypeEnum.BATCH.toString())) {
					deleteBatchUseCaseHandler.deleteBatchDigitalTwinsAndEDC(processId, delProcessId);
				}
			} catch (JsonProcessingException e) {
				throw new DftException(e);
			}
		};
		new Thread(runnable).start();

		return ok().body(delProcessId);
	}
}
