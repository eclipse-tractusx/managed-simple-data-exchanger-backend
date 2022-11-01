package com.catenax.sde.core.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.catenax.sde.common.entities.SubmodelFileRequest;
import com.catenax.sde.common.entities.SubmodelJsonRequest;
import com.catenax.sde.common.exception.ServiceException;
import com.catenax.sde.common.validators.UsagePolicyValidation;
import com.catenax.sde.core.csv.service.CsvHandlerService;
import com.catenax.sde.core.cvs.entities.CsvContent;
import com.catenax.sde.core.service.SubmodelOrchestartorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
public class SubmodelProcessController {

	private final SubmodelOrchestartorService submodelOrchestartorService;

	private final CsvHandlerService csvHandlerService;

	private ObjectMapper objectMapper = new ObjectMapper();

	@PostMapping(value = "/submodels/{submodel}/upload")
	public ResponseEntity<String> fileUpload(@PathVariable("submodel") String submodel,
			@RequestParam("file") MultipartFile file, @UsagePolicyValidation @RequestParam("meta_data") String metaData)
			throws JsonProcessingException {

		String processId = csvHandlerService.storeFile(file);

		SubmodelFileRequest submodelFileRequest = objectMapper.readValue(metaData, SubmodelFileRequest.class);

		Runnable runnable = () -> {
			CsvContent csvContent = csvHandlerService.processFile(processId, submodel);
			submodelOrchestartorService.processSubmodelCsv(csvContent, submodelFileRequest, processId, submodel);
		};

		new Thread(runnable).start();

		return ok().body(processId);
	}

	@PostMapping(value = "/submodels/{submodel}", consumes = APPLICATION_JSON_VALUE)
	public ResponseEntity<String> createAspectBatch(@PathVariable("submodel") String submodel,
			@RequestBody SubmodelJsonRequest<JsonObject> submodelJsonRequest) {

		String processId = UUID.randomUUID().toString();

		Runnable runnable = () -> {
			try {
				submodelOrchestartorService.processSubmodel(submodelJsonRequest, processId, submodel);
			} catch (ServiceException e) {
				throw new ServiceException(e.getMessage());
			}
		};
		new Thread(runnable).start();

		return ok().body(processId);
	}
}
