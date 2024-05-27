/********************************************************************************
 * Copyright (c) 2024 T-Systems International GmbH
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

import java.util.Map;

import org.eclipse.tractusx.sde.common.utils.LogUtil;
import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFRequestStatusEnum;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFTypeEnum;
import org.eclipse.tractusx.sde.pcfexchange.request.PcfRequestModel;
import org.eclipse.tractusx.sde.pcfexchange.service.IPCFExchangeService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("pcf")
public class PcfExchangeController {

	private final IPCFExchangeService pcfExchangeService;

	@PostMapping(value = "/request/{productId}")
	@PreAuthorize("hasPermission('','request_for_pcf_value')")
	public ResponseEntity<Object> requestForPcfDataOffer(@PathVariable String productId,
			@Valid @RequestBody ConsumerRequest consumerRequest) throws Exception {
		log.info(LogUtil.encode("Request received for POST: /api/pcf/request/" + productId));
		return ok().body(Map.of("msg", pcfExchangeService.requestForPcfDataExistingOffer(productId, consumerRequest)));
	}
	
	@PostMapping(value = "/request/nonexistdataoffer")
	@PreAuthorize("hasPermission('','request_for_pcf_value')")
	public ResponseEntity<Object> requestForPcfNotExistDataOffer(@Valid @RequestBody PcfRequestModel pcfRequestModel)
			throws Exception {
		log.info(LogUtil.encode("Request received for POST: /api/pcf/nonexistdataoffer/" + pcfRequestModel.getProductId()));
		return ok().body(Map.of("msg",
				pcfExchangeService.requestForPcfNotExistDataOffer(pcfRequestModel)));
	}
	
	@GetMapping(value = "/request/{requestId}")
	@PreAuthorize("hasPermission('','request_for_pcf_value')")
	public ResponseEntity<Object> viewForPcfDataOffer(@PathVariable String requestId) throws Exception {
		log.info(LogUtil.encode("Request received for GET: /request/" + requestId));
		return ok().body(pcfExchangeService.viewForPcfDataOffer(requestId));
	}

	@PostMapping(value = "/actionsonrequest")
	@PreAuthorize("hasPermission('','action_on_pcf_request')")
	public ResponseEntity<Object> actionOnPcfRequestAndSendNotificationToConsumer(
			@Valid @RequestBody PcfRequestModel pcfRequestModel) throws Exception {

		log.info("Request received for POST: /request/actionsonrequest");

		String msg = pcfExchangeService.actionOnPcfRequestAndSendNotificationToConsumer(pcfRequestModel);

		return ok().body(Map.of("msg", msg));
	}
	
	@GetMapping(value = "/{type}/requests")
	@PreAuthorize("hasPermission('','view_pcf_history')")
	public ResponseEntity<Object> getPcfProviderData(@PathVariable PCFTypeEnum type,
			@RequestParam(value = "status", required = false) PCFRequestStatusEnum status, @RequestParam("offset") Integer page,
			@RequestParam("maxLimit") Integer pageSize) throws Exception {
		log.info("Request received for GET: /api/pcf/{}/requests/", type.name().toLowerCase());

		page = page == null ? 0 : page;
		pageSize = pageSize == null ? 10 : pageSize;

		return ok().body(pcfExchangeService.getPcfData(status, type, page, pageSize));
	}

	// PCF data exchange api's
	@GetMapping(value = "/productIds/{productId}")
	public ResponseEntity<Object> getPcfByProduct(@PathVariable String productId,
			@RequestHeader(value = "Edc-Bpn") String bpnNumber,
			@RequestParam(value = "requestId") String requestId, 
			@RequestParam(value = "message", required = false) String message)
			throws Exception {
		log.info(LogUtil.encode("Request received for GET: /api/pcf/productIds/" + productId));
		pcfExchangeService.savePcfRequestData(requestId, productId, bpnNumber, message);
		return ResponseEntity.accepted().body(Map.of("msg", "PCF request accepted"));
	}

	@PutMapping(value = "/productIds/{productId}")
	public ResponseEntity<Object> uploadPcfSubmodel(@PathVariable String productId,
			@RequestHeader(value = "Edc-Bpn") String bpnNumber,
			@RequestParam(value = "requestId", required = false) String requestId,
			@RequestParam(value = "message", required = false) String message, 
			@RequestBody JsonNode pcfData) {
		log.info(LogUtil.encode("Request received for PUT: /api/pcf/productIds/" + productId));

		pcfExchangeService.recievedPCFData(productId, bpnNumber, requestId, message, pcfData);
		return ResponseEntity.ok().body(Map.of("msg", "PCF response recieved"));
	}
}
