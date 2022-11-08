package com.catenax.sde.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catenax.sde.common.enums.CsvTypeEnum;
import com.catenax.sde.entities.database.AspectEntity;
import com.catenax.sde.entities.usecases.ProcessReport;
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

    private final GetAspectsUseCase aspectsUseCase;
    private final GetAspectsRelationshipUseCase aspectsRelationshipUseCase;
    private final DeleteUsecaseHandler deleteUsecaseHandler;
	
    
	public DeleteController(GetAspectsUseCase aspectsUseCase,
			GetAspectsRelationshipUseCase aspectsRelationshipUseCase,DeleteUsecaseHandler deleteUsecaseHandler) {
		super();
		this.aspectsUseCase = aspectsUseCase;
		this.aspectsRelationshipUseCase = aspectsRelationshipUseCase;
		this.deleteUsecaseHandler=deleteUsecaseHandler;
	}

	@DeleteMapping(value = "/{processId}/{csvType}", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<String> deleteRecordsWithDigitalTwinAndEDC(@PathVariable("processId") String processId,
			@PathVariable("csvType") String csvType) throws JsonProcessingException {
		String response = "";

		if (csvType.equalsIgnoreCase(CsvTypeEnum.ASPECT.toString())) {
			List<AspectEntity> listAspect = aspectsUseCase.getListUuidFromProcessId(processId);
			if (listAspect.size() > 0) {

				response = deleteUsecaseHandler.deleteAspectDigitalTwinsAndEDC(listAspect, processId);
			} else
				return ok().body("No id associate with processId");

		} else if (csvType.equals(CsvTypeEnum.ASPECT_RELATIONSHIP.toString())) {

		} else if (csvType.equals(CsvTypeEnum.BATCH.toString())) {

		}

		return ok().body(response);
	}
}
