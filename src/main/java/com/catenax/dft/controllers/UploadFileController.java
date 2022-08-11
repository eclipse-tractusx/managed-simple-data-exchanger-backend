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

package com.catenax.dft.controllers;

import static org.springframework.http.ResponseEntity.ok;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.catenax.dft.entities.SubmodelFileRequest;
import com.catenax.dft.entities.csv.CsvContent;
import com.catenax.dft.gateways.file.CsvGateway;
import com.catenax.dft.usecases.csvhandler.CsvHandlerOrchestrator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class UploadFileController {

	private final CsvGateway csvGateway;
	private final CsvHandlerOrchestrator csvHandlerOrchestrator;
	
	private ObjectMapper objectMapper = new ObjectMapper();
	

	public UploadFileController(CsvGateway storageService, CsvHandlerOrchestrator csvHandlerOrchestrator) {
		this.csvGateway = storageService;
		this.csvHandlerOrchestrator = csvHandlerOrchestrator;
	}

	@PostMapping(value = "/upload")
	public ResponseEntity<String> fileUpload(@RequestParam("file") MultipartFile file,
			@RequestParam("meta_data") String metaData) throws JsonProcessingException  {
		String processId = csvGateway.storeFile(file);

		SubmodelFileRequest submodelFileRequest = objectMapper.readValue(metaData, SubmodelFileRequest.class);
		
		Runnable runnable = () -> {
			CsvContent csvContent = csvGateway.processFile(processId);
			csvHandlerOrchestrator.execute(csvContent, processId, submodelFileRequest);
		};

		new Thread(runnable).start();

		return ok().body(processId);
	}
}