/********************************************************************************
 * Copyright (c) 2022, 2024 T-Systems International GmbH
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.core.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.tractusx.sde.common.entities.PolicyTemplateRequest;
import org.eclipse.tractusx.sde.common.entities.SubmodelJsonRequest;
import org.eclipse.tractusx.sde.common.mapper.PolicyTemplateObjectMapper;
import org.eclipse.tractusx.sde.common.validators.ValidatePolicyTemplate;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.core.service.SubmodelOrchestartorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequiredArgsConstructor
public class SubmodelProcessController {

	private final SubmodelOrchestartorService submodelOrchestartorService;

	private final CsvHandlerService csvHandlerService;

	private final PolicyTemplateObjectMapper mapper;

	@PostMapping(value = "/{submodel}/upload")
	@PreAuthorize("hasPermission(#submodel,'provider_create_contract_offer@provider_update_contract_offer')")
	public ResponseEntity<Object> upload(@PathVariable("submodel") String submodel,
			@RequestParam("file") MultipartFile file,
			@RequestParam("meta_data") @Valid @ValidatePolicyTemplate String metaData) {

		String processId = csvHandlerService.storeFile(file);

		PolicyTemplateRequest policyTemplateRequest = mapper.strToObject(metaData);

		submodelOrchestartorService.processSubmodelCsv(policyTemplateRequest, processId, submodel);

		return prepareAndReturnResponse(processId);
	}

	@PostMapping(value = "/{submodel}/manualentry", consumes = APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission(#submodel,'provider_create_contract_offer@provider_update_contract_offer')")
	public ResponseEntity<Object> manualentry(@PathVariable("submodel") String submodel,
			@RequestBody @Valid @ValidatePolicyTemplate SubmodelJsonRequest body) {

		String processId = UUID.randomUUID().toString();

		submodelOrchestartorService.processSubmodel(body, processId, submodel);

		return prepareAndReturnResponse(processId);
	}

	@GetMapping(value = "/{submodel}/public/{uuid}")
	public ResponseEntity<Map<Object, Object>> readCreatedTwinsDetails(@PathVariable("submodel") String submodel,
			@PathVariable("uuid") String uuid,
			@RequestParam(value = "type", defaultValue = "json", required = false) String type) {
		return ok().body(submodelOrchestartorService.readCreatedTwinsDetails(submodel, uuid, type));
	}

	@DeleteMapping(value = "/{submodel}/delete/{processId}", produces = APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission(#submodel,'provider_delete_contract_offer')")
	public ResponseEntity<Object> deleteRecordsWithDigitalTwinAndEDC(
			@PathVariable("processId") String processId, @PathVariable("submodel") String submodel) {
		String delProcessId = UUID.randomUUID().toString();

		submodelOrchestartorService.deleteSubmodelDigitalTwinsAndEDC(processId, delProcessId, submodel);
		
		return prepareAndReturnResponse(delProcessId);
	}
	
	private ResponseEntity<Object> prepareAndReturnResponse(String processId) {
		Map<String, String> res = new HashMap<>();
		res.put("processId", processId);
		return ok().body(res);
	}
}
