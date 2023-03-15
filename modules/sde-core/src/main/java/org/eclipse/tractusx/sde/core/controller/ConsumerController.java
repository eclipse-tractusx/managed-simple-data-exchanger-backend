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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.edc.services.ConsumerControlPanelService;
import org.eclipse.tractusx.sde.portal.model.ConnectorInfo;
import org.eclipse.tractusx.sde.portal.model.response.LegalEntityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ConsumerController {

	@Autowired
	private final ConsumerControlPanelService consumerControlPanelService;

	public ConsumerController(ConsumerControlPanelService consumerControlPanelService) {
		this.consumerControlPanelService = consumerControlPanelService;
	}

	@GetMapping(value = "/query-data-offers")
	@PreAuthorize("hasPermission('','consumer_view_contract_offers')")
	public ResponseEntity<Object> queryOnDataOffers(@RequestParam String providerUrl) throws Exception {
		log.info("Request received : /api/query-data-Offers");
		return ok().body(consumerControlPanelService.queryOnDataOffers(providerUrl));
	}

	@PostMapping(value = "/subscribe-data-offers")
	@PreAuthorize("hasPermission('','consumer_establish_contract_agreement')")
	public ResponseEntity<Object> subscribeDataOffers(@Valid @RequestBody ConsumerRequest consumerRequest) {
		String processId = UUID.randomUUID().toString();
		log.info("Request recevied : /api/subscribe-data-offers");
		consumerControlPanelService.subscribeDataOffers(consumerRequest, processId);
		return ResponseEntity.ok().body(processId);
	}

	@GetMapping(value = "/contract-agreements", produces = APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission('','consumer_view_contract_agreement@provider_view_contract_agreement')")
	public ResponseEntity<Object> queryOnDataOffersStatus(@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "maxLimit", required = false) Integer limit,
			@RequestParam(value = "offset", required = false) Integer offset) {
		log.info("Request received : /api/contract-agreements");
		if (limit == null) {
			limit = 10;
		}
		if (offset == null) {
			offset = 0;
		}
		Map<String,Object> res = consumerControlPanelService.getAllContractOffers(type, limit,
				offset);
		return ok().body(res);
	}

	@GetMapping(value = "/legal-entities")
	@PreAuthorize("hasPermission('','consumer_search_connectors')")
	public ResponseEntity<List<LegalEntityResponse>> fetchLegalEntitiesData(@RequestParam String searchText,
			@RequestParam Integer page, @RequestParam Integer size) throws Exception {
		log.info("Request received : /api/legal-entities");
		List<LegalEntityResponse> legalEntitiesResponse = consumerControlPanelService.fetchLegalEntitiesData(searchText,
				page, size);
		return ok().body(legalEntitiesResponse);
	}

	@PostMapping(value = "/connectors-discovery")
	@PreAuthorize("hasPermission('','consumer_search_connectors')")
	public ResponseEntity<List<ConnectorInfo>> fetchConnectorInfo(@RequestBody List<String> bpns) throws Exception {
		log.info("Request received : /api/connectors-discovery");
		List<ConnectorInfo> fetchConnectorInfoResponse = consumerControlPanelService.fetchConnectorInfo(bpns);
		return ok().body(fetchConnectorInfoResponse);
	}
}
