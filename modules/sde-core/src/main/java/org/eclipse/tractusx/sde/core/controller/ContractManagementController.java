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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import java.util.Map;

import org.eclipse.tractusx.sde.edc.enums.Type;
import org.eclipse.tractusx.sde.edc.facilitator.ContractNegotiateManagementHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("contract-agreements")
@RequiredArgsConstructor
public class ContractManagementController {

	private final ContractNegotiateManagementHelper contractNegotiateManagement;

	@GetMapping(value = "/provider", produces = APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission('','provider_view_contract_agreement')")
	public ResponseEntity<Object> contractAgreementsProvider(
			@RequestParam(value = "maxLimit", required = false) Integer limit,
			@RequestParam(value = "offset", required = false) Integer offset) {
		if (limit == null) {
			limit = 10;
		}
		if (offset == null) {
			offset = 0;
		}
		Map<String, Object> res = contractNegotiateManagement.getAllContractOffers(Type.PROVIDER.name(), limit, offset);
		return ok().body(res);
	}

	@GetMapping(value = "/consumer", produces = APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission('','consumer_view_contract_agreement')")
	public ResponseEntity<Object> contractAgreementsConsumer(
			@RequestParam(value = "maxLimit", required = false) Integer limit,
			@RequestParam(value = "offset", required = false) Integer offset) {
		if (limit == null) {
			limit = 10;
		}
		if (offset == null) {
			offset = 0;
		}
		Map<String, Object> res = contractNegotiateManagement.getAllContractOffers(Type.CONSUMER.name(), limit, offset);
		return ok().body(res);
	}

	@PostMapping(value = "/{negotiationId}/provider/decline", produces = APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission('','provider_delete_contract_agreement')")
	public ResponseEntity<Object> declineContractProvider(@PathVariable("negotiationId") String negotiationId) {
		contractNegotiateManagement.declineContract(Type.PROVIDER.name(), negotiationId);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@GetMapping(value = "/{negotiationId}/consumer/decline", produces = APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission('','consumer_delete_contract_agreement')")
	public ResponseEntity<Object> declineContractConsumer(@PathVariable("negotiationId") String negotiationId) {
		contractNegotiateManagement.declineContract(Type.CONSUMER.name(), negotiationId);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@PostMapping(value = "/{negotiationId}/provider/cancel", produces = APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission('','provider_delete_contract_agreement')")
	public ResponseEntity<Object> cancelContractProvider(@PathVariable("negotiationId") String negotiationId) {
		contractNegotiateManagement.cancelContract(Type.PROVIDER.name(), negotiationId);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@PostMapping(value = "/{negotiationId}/consumer/cancel", produces = APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission('','consumer_delete_contract_agreement')")
	public ResponseEntity<Object> cancelContractConsumer(@PathVariable("negotiationId") String negotiationId) {
		contractNegotiateManagement.cancelContract(Type.CONSUMER.name(), negotiationId);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}
}
