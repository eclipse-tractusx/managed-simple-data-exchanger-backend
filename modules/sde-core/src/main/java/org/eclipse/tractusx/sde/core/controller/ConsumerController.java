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

import static org.springframework.http.ResponseEntity.ok;

import java.util.List;
import java.util.UUID;

import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.core.service.ConsumerService;
import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.edc.model.request.QueryDataOfferRequest;
import org.eclipse.tractusx.sde.edc.services.ConsumerControlPanelService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ConsumerController {

	private final ConsumerControlPanelService consumerControlPanelService;

	private final ConsumerService consumerService;

	@GetMapping(value = "/query-data-offers")
	@PreAuthorize("hasPermission('','consumer_view_contract_offers')")
	public ResponseEntity<Object> queryOnDataOffers(
			@RequestParam(value = "manufacturerPartId", required = false) String manufacturerPartId,
			@RequestParam(value = "bpnNumber", required = false) String bpnNumber,
			@RequestParam(value = "submodel", required = false) String submodel,
			@RequestParam(value = "maxLimit", required = false) Integer limit,
			@RequestParam(value = "offset", required = false) Integer offset) throws Exception {
		log.info("Request received : /api/query-data-Offers");

		if (StringUtils.isBlank(manufacturerPartId) && StringUtils.isBlank(bpnNumber))
			throw new ValidationException(
					"At least one request attribute between 'manufacturerPartId' or company 'bpnNumber' needs to provide for search");

		if (limit == null) {
			limit = 10;
		}
		if (offset == null) {
			offset = 0;
		}
		
		return ok().body(
				consumerControlPanelService.queryOnDataOffers(manufacturerPartId, bpnNumber, submodel, offset, limit));
	}

	@PostMapping(value = "/offer-policy-details")
	@PreAuthorize("hasPermission('','consumer_view_contract_offers')")
	public ResponseEntity<Object> getEDCPolicy(@RequestBody List<QueryDataOfferRequest> queryDataOfferRequest)
			throws Exception {
		log.info("Request received : /api/offer-policy-details");
		return ok().body(consumerControlPanelService.getEDCPolicy(queryDataOfferRequest));
	}
	
	@PostMapping(value = "/subscribe-data-offers")
	@PreAuthorize("hasPermission('','consumer_establish_contract_agreement')")
	public ResponseEntity<Object> subscribeDataOffers(@Valid @RequestBody ConsumerRequest consumerRequest) {
		String processId = UUID.randomUUID().toString();
		log.info("Request recevied : /api/subscribe-data-offers");
		consumerControlPanelService.subscribeDataOffers(consumerRequest, processId);
		return ResponseEntity.ok().body(processId);
	}

	@PostMapping(value = "/subscribe-download-data-offers-async")
	@PreAuthorize("hasPermission('','consumer_subscribe_download_data_offers')")
	public ResponseEntity<Object> subscribeAndDownloadDataOffersAsync(
			@Valid @RequestBody ConsumerRequest consumerRequest) {
		log.info("Request recevied : /api/subscribe-download-data-offers-async");
		return ResponseEntity.ok().body(consumerService.subscribeAndDownloadDataOffersAsync(consumerRequest));
	}

	@PostMapping(value = "/subscribe-download-data-offers")
	@PreAuthorize("hasPermission('','consumer_subscribe_download_data_offers')")
	public void subscribeAndDownloadDataOffersSynchronous(@Valid @RequestBody ConsumerRequest consumerRequest,
			HttpServletResponse response) {
		log.info("Request recevied : /api/subscribe-download-data-offers");
		consumerService.subscribeAndDownloadDataOffersSynchronous(consumerRequest, response);
	}

	@GetMapping(value = "/download-data-offers")
	@PreAuthorize("hasPermission('','consumer_download_data_offer')")
	public void downloadFileFromEDCUsingifAlreadyTransferStatusCompleted(
			@RequestParam("processId") String referenceProcessId,
			@RequestParam(value = "type", defaultValue = "csv", required = false) String type,
			HttpServletResponse response) throws Exception {
		log.info("Request received : /api/download-data-offers");
		consumerService.downloadFileFromEDCUsingifAlreadyTransferStatusCompleted(referenceProcessId, type, response);
	}

	@GetMapping(value = "/view-download-history")
	@PreAuthorize("hasPermission('','consumer_view_download_history')")
	public ResponseEntity<Object> viewConsumerDownloadHistory(@Param("page") Integer page,
			@Param("pageSize") Integer pageSize) throws Exception {
		page = page == null ? 0 : page;
		pageSize = pageSize == null ? 10 : pageSize;
		log.info("Request received : /api/view-download-history");
		return ok().body(consumerService.viewDownloadHistory(page, pageSize));
	}

	@GetMapping(value = "/view-download-history/{processId}")
	@PreAuthorize("hasPermission('','consumer_view_download_history')")
	public ResponseEntity<Object> viewConsumerDownloadHistoryDetails(@PathVariable("processId") String processId)
			throws Exception {
		log.info("Request received : /api/view-download-history-details");
		return ok().body(consumerService.viewConsumerDownloadHistoryDetails(processId));
	}
}
