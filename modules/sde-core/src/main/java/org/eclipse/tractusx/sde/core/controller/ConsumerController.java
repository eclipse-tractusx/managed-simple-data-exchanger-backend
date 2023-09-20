/********************************************************************************
 * Copyright (c) 2022, 2023 T-Systems International GmbH
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import org.eclipse.tractusx.sde.core.service.ConsumerService;
import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.edc.services.ConsumerControlPanelService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
	public ResponseEntity<Object> queryOnDataOffers(@RequestParam String providerUrl,
			@RequestParam(value = "maxLimit", required = false) Integer limit,
			@RequestParam(value = "offset", required = false) Integer offset) throws Exception {
		log.info("Request received : /api/query-data-Offers");
		if (limit == null) {
			limit = 10;
		}
		if (offset == null) {
			offset = 0;
		}
		return ok().body(consumerControlPanelService.queryOnDataOffers(providerUrl, offset, limit, null));
	}

	@PostMapping(value = "/subscribe-data-offers")
	@PreAuthorize("hasPermission('','consumer_establish_contract_agreement')")
	public ResponseEntity<Object> subscribeDataOffers(@Valid @RequestBody ConsumerRequest consumerRequest) {
		String processId = UUID.randomUUID().toString();
		log.info("Request recevied : /api/subscribe-data-offers");
		consumerControlPanelService.subscribeDataOffers(consumerRequest, processId);
		return ResponseEntity.ok().body(processId);
	}

	@PostMapping(value = "/subscribe-download-data-offers")
	@PreAuthorize("hasPermission('','consumer_download_data')")
	public void subscribeAndDownloadDataOffers(@Valid @RequestBody ConsumerRequest consumerRequest, HttpServletResponse response) {
		log.info("Request recevied : /api/subscribe-download-data-edr");
		consumerService.subscribeAndDownloadDataOffers(consumerRequest, response);
	}

	@GetMapping(value = "/download-data-offers")
	@PreAuthorize("hasPermission('','consumer_download_data')")
	public ResponseEntity<Object> downloadFileFromEDCUsingifAlreadyTransferStatusCompleted(@RequestParam List<String> assetIdList)
			throws Exception {
		log.info("Request received : /api/download-data-using-edr");
		return consumerService.downloadFileFromEDCUsingifAlreadyTransferStatusCompleted(assetIdList);
	}

}
