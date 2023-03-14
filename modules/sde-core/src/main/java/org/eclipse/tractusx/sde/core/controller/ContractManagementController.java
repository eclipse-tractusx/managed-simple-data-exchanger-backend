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

	@PostMapping(value = "/{negotiationId}/decline/provider", produces = APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission('','provider_delete_contract_agreement')")
	public ResponseEntity<Object> declineContractProvider(@PathVariable("negotiationId") String negotiationId) {
		contractNegotiateManagement.declineContract(Type.PROVIDER.name(), negotiationId);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@GetMapping(value = "/{negotiationId}/decline/consumer", produces = APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission('','consumer_delete_contract_agreement')")
	public ResponseEntity<Object> declineContractConsumer(@PathVariable("negotiationId") String negotiationId) {
		contractNegotiateManagement.declineContract(Type.CONSUMER.name(), negotiationId);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@PostMapping(value = "/{negotiationId}/cancel/provider", produces = APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission('','provider_delete_contract_agreement')")
	public ResponseEntity<Object> cancelContractProvider(@PathVariable("negotiationId") String negotiationId) {
		contractNegotiateManagement.cancelContract(Type.PROVIDER.name(), negotiationId);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@PostMapping(value = "/{negotiationId}/cancel/consumer", produces = APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission('','consumer_delete_contract_agreement')")
	public ResponseEntity<Object> cancelContractConsumer(@PathVariable("negotiationId") String negotiationId) {
		contractNegotiateManagement.cancelContract(Type.CONSUMER.name(), negotiationId);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}
}
