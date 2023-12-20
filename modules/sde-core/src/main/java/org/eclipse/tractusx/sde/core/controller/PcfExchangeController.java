/********************************************************************************
 * Copyright (c) 2023 T-Systems International GmbH
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import static org.springframework.http.ResponseEntity.ok;

import org.eclipse.tractusx.sde.common.entities.SubmodelJsonRequest;
import org.eclipse.tractusx.sde.common.validators.ValidatePolicyTemplate;
import org.eclipse.tractusx.sde.pcfexchange.response.PcfExchangeResponse;
import org.eclipse.tractusx.sde.pcfexchange.service.impl.PcfExchangeServiceImpl;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("pcf")
public class PcfExchangeController {
	
	private final PcfExchangeServiceImpl pcfExchangeService;
	
	@GetMapping(value = "/data-offer/{productId}")
	public ResponseEntity<PcfExchangeResponse> getPcfDataOfferByProduct(@PathVariable String productId,
			@RequestParam(value = "BPN", required = true) String bpnNumber) throws Exception {
		log.info("Request received for GET: /api/pcf/data-offer");
		
		return ResponseEntity.ok().body(pcfExchangeService.findPcfDataOffer(productId, bpnNumber));
		
	}
	
	@GetMapping(value = "/exchange/request/{productId}")
	public ResponseEntity<Object> savePcfRequestForProductId(@PathVariable String productId,
			@RequestParam(value = "BPN", required = true) String bpnNumber, 
			@RequestParam(value = "requestId", required = true) String requestId,
			@RequestParam String message) throws Exception {
		log.info("Request received for POST: /api/pcf/request/productIds");
		
		return ResponseEntity.accepted().body(null);
	}

	
	@GetMapping(value = "/all/requests")
	public ResponseEntity<Object> allPcfRequest(@RequestParam(value = "type", required = false) String type,@Param("page") Integer page, @Param("pageSize") Integer pageSize) throws Exception {
		log.info("Request received for POST: /api/pcf/request/productIds");
		
		page = page == null ? 0 : page;
		pageSize = pageSize == null ? 10 : pageSize;
		
		return ok().body(pcfExchangeService.getAllPcfRequestData(type, page,  pageSize));
	}
	
	// PCF data exchange api's
	@GetMapping(value = "/productIds/{productId}")
	public ResponseEntity<Object> getPcfByProduct(@PathVariable String productId,
			@RequestParam(value = "BPN", required = true) String bpnNumber,
			@RequestParam(value = "requestId", required = true) String requestId,
			@RequestParam String message) throws Exception {
		log.info("Request received for GET: /api/pcf/productIds");
		
		return ResponseEntity.accepted().body(pcfExchangeService.savePcfRequestData(requestId, productId, bpnNumber, message).getRequestId());
	}
	
	
	@PutMapping(value = "/productIds/{productId}")
	public ResponseEntity<Object> uploadPcfSubmodel(@PathVariable String productId,
			@RequestParam(value = "BPN", required = true) String bpnNumber, 
			@RequestParam(value = "requestId", required = true) String requestId,
			@RequestParam(value = "message", required = false) String message,
			@RequestBody @Valid @ValidatePolicyTemplate SubmodelJsonRequest pcfSubmodelJsonRequest) {
		log.info("Request received for PUT: /api/pcf/productIds");
		
		pcfExchangeService.approveAndPushPCFData(productId, bpnNumber,requestId, message, pcfSubmodelJsonRequest);
		return ok().body(null);
	}
}
