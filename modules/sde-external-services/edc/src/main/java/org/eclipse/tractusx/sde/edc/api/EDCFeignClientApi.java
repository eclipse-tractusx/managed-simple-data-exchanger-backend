package org.eclipse.tractusx.sde.edc.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.http.HttpHeaders;

@FeignClient(value = "EDCFeignClientApi", url = "${edc.hostname}")
public interface EDCFeignClientApi {

	@DeleteMapping(path = "/contractdefinitions/{id}")
	ResponseEntity<Object> deleteContractDefinition(@PathVariable("id") String contractdefinitionsId,
			@RequestHeader HttpHeaders requestHeader);

	@DeleteMapping(path = "/policydefinitions/{id}")
	ResponseEntity<Object> deletePolicyDefinitions(@PathVariable("id") String policydefinitionsId,
			@RequestHeader HttpHeaders requestHeader);

	@DeleteMapping(path = "/assets/{id}")
	ResponseEntity<Object> deleteAssets(@PathVariable("id") String assetsId, @RequestHeader HttpHeaders requestHeader);

}
	