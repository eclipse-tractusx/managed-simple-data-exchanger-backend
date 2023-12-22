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

import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFTypeEnum;
import org.eclipse.tractusx.sde.pcfexchange.request.PcfRequestModel;
import org.eclipse.tractusx.sde.pcfexchange.service.impl.PcfExchangeServiceImpl;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("pcf")
public class PcfExchangeController {

	private final PcfExchangeServiceImpl pcfExchangeService;

	@GetMapping(value = "/search")
	public ResponseEntity<Object> searchPcfDataOffer(@RequestParam String manufacturerPartId,
			@RequestParam String bpnNumber) throws Exception {
		log.info("Request received for GET: /api/pcf/search");
		
		List<QueryDataOfferModel> pcfOffer = pcfExchangeService.searchPcfDataOffer(manufacturerPartId, bpnNumber);
		if (pcfOffer == null || pcfOffer.isEmpty())
			return ResponseEntity.ok().body(Map.of("msg", "No PCF twin found"));
		else
			return ResponseEntity.ok().body(pcfOffer);

	}

	@GetMapping(value = "/request/{productId}")
	public ResponseEntity<Object> getPcfDataOfferByProduct(@PathVariable String productId,
			@Valid @RequestBody ConsumerRequest consumerRequest) throws Exception {
		log.info("Request received for GET: /api/pcf/request/");
		return ResponseEntity.ok().body(pcfExchangeService.requestForPcfDataOffer(productId, consumerRequest));
	}

	@PostMapping(value = "/approve/{productId}")
	public ResponseEntity<Object> pcfApprove(@PathVariable String productId, @RequestParam String bpnNumber,
			@RequestParam String requestId, @RequestParam String message) throws Exception {

		log.info("Request received for POST: /api/pcf/approve/");

		pcfExchangeService.approveAndPushPCFData(productId, bpnNumber, requestId, message);

		return ResponseEntity.accepted().body(Map.of("msg", "PCF request approved and async pcf data pushed"));
	}

	@GetMapping(value = "/provider/requests")
	public ResponseEntity<Object> getPcfProviderData(@RequestParam(value = "status", required = false) String status,
			@Param("page") Integer page, @Param("pageSize") Integer pageSize) throws Exception {
		log.info("Request received for POST: /api/pcf/requests/");

		page = page == null ? 0 : page;
		pageSize = pageSize == null ? 10 : pageSize;

		return ok().body(pcfExchangeService.getPcfData(status, PCFTypeEnum.PROVIDER, page, pageSize));
	}
	
	@GetMapping(value = "/consumer/requests")
	public ResponseEntity<Object> getPcfConsumerData(@RequestParam(value = "status", required = false) String status,
			@Param("page") Integer page, @Param("pageSize") Integer pageSize) throws Exception {
		log.info("Request received for POST: /api/pcf/requests/");

		page = page == null ? 0 : page;
		pageSize = pageSize == null ? 10 : pageSize;

		return ok().body(pcfExchangeService.getPcfData(status, PCFTypeEnum.CONSUMER, page, pageSize));
	}

	// PCF data exchange api's
	@GetMapping(value = "/productIds/{productId}")
	public ResponseEntity<Object> getPcfByProduct(@PathVariable String productId,
			@RequestParam(value = "BPN", required = true) String bpnNumber,
			@RequestParam(value = "requestId", required = true) String requestId, @RequestParam String message)
			throws Exception {
		log.info("Request received for GET: /api/pcf/productIds");
		pcfExchangeService.savePcfRequestData(requestId, productId, bpnNumber, message, PCFTypeEnum.PROVIDER);
		return ResponseEntity.accepted().body(Map.of("msg", "PCF request accepted"));
	}

	@PutMapping(value = "/productIds/{productId}")
	public ResponseEntity<Object> uploadPcfSubmodel(@PathVariable String productId,
			@RequestParam(value = "BPN", required = true) String bpnNumber,
			@RequestParam(value = "requestId", required = false) String requestId,
			@RequestParam(value = "message", required = false) String message, @RequestBody JsonObject pcfData) {
		log.info("Request received for PUT: /api/pcf/productIds");

		pcfExchangeService.recievedPCFData(productId, bpnNumber, requestId, message, pcfData);
		return ResponseEntity.ok().body(Map.of("msg", "PCF response recieved"));
	}
}
