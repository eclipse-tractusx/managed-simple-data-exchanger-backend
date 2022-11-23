package org.eclipse.tractusx.sde.digitaltwins.gateways.external;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "DigitalTwinsFeignClient", url = "${digital-twins.hostname}")
public interface DigitalTwinsFeignClient {
	
	  @DeleteMapping(path = "/lookup/shells/{id}")
	  ResponseEntity<Object> deleteDigitalTwinsById(@PathVariable("id") String shellId,
              @RequestHeader Map<String, String> requestHeader);


}
