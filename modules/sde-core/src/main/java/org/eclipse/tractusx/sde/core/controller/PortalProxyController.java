/********************************************************************************
 * Copyright (c) 2023,2024 T-Systems International GmbH
 * Copyright (c) 2023,2024 Contributors to the Eclipse Foundation
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

import org.eclipse.tractusx.sde.core.service.PartnerPoolService;
import org.eclipse.tractusx.sde.portal.handler.PortalProxyService;
import org.eclipse.tractusx.sde.portal.model.ConnectorInfo;
import org.eclipse.tractusx.sde.portal.model.response.LegalEntityResponse;
import org.eclipse.tractusx.sde.portal.model.response.UnifiedBpnValidationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class PortalProxyController {

	@Autowired
	private PortalProxyService portalProxyService;
	
	@Autowired
	private PartnerPoolService partnerPoolService;
	
	
	@GetMapping(value = "/legal-entities")
	@PreAuthorize("hasPermission('','consumer_search_connectors')")
	public ResponseEntity<List<LegalEntityResponse>> fetchLegalEntitiesData(@RequestParam (required = false) String bpnLs, @RequestParam String searchText,
			@RequestParam Integer page, @RequestParam Integer size) throws Exception {
		log.info("Request received : /api/legal-entities");
		List<LegalEntityResponse> legalEntitiesResponse = partnerPoolService.fetchLegalEntitiesData(bpnLs, searchText,
				page, size);
		return ok().body(legalEntitiesResponse);
	}

	@PostMapping(value = "/connectors-discovery")
	@PreAuthorize("hasPermission('','consumer_search_connectors')")
	public ResponseEntity<List<ConnectorInfo>> fetchConnectorInfo(@RequestBody List<String> bpns) throws Exception {
		log.info("Request received : /api/connectors-discovery");
		List<ConnectorInfo> fetchConnectorInfoResponse = portalProxyService.fetchConnectorInfo(bpns);
		return ok().body(fetchConnectorInfoResponse);
	}
	
	@GetMapping(value = "/unified-bpn-validation/{bpn}")
	@PreAuthorize("hasPermission('','unified_bpn_validation')")
	public ResponseEntity<UnifiedBpnValidationResponse> unifiedBpnValidation(@PathVariable("bpn") String bpn) throws Exception {
		log.info("Request received : /api/unified-bpn-validation");
		return ok().body(portalProxyService.unifiedBpnValidation(bpn));
	}

}
