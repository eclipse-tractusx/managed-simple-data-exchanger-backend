package org.eclipse.tractusx.sde.pcfexchange.proxy;

import java.net.URI;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.JsonNode;

@FeignClient(name = "PCFExchangeProxy", url = "placeholder")
public interface PCFExchangeProxy {

	@PutMapping(value = "/productIds/{productId}")
	public ResponseEntity<Object> uploadPcfSubmodel(URI url, @RequestHeader Map<String, String> requestHeader,
			@PathVariable("productId") String productId, @RequestParam(value = "BPN", required = true) String bpnNumber,
			@RequestParam(value = "requestId", required = false) String requestId,
			@RequestParam(value = "message", required = false) String message, @RequestBody JsonNode pcfData);

	@GetMapping
	public ResponseEntity<Object> getPcfByProduct(URI url, @RequestHeader Map<String, String> requestHeader, @RequestParam(value = "BPN", required = true) String bpnNumber,
			@RequestParam(value = "requestId", required = true) String requestId, @RequestParam String message);

}
