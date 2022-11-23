package org.eclipse.tractusx.sde.portal.api;

import org.eclipse.tractusx.sde.portal.model.ConnectorInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


@FeignClient(value = "ConnectorDiscoveryApi", url = "${portal.backend.hostname}")
public interface ConnectorDiscoveryApi {
    @PostMapping(path = "/api/administration/Connectors/discovery")
    ResponseEntity<ConnectorInfo[]> fetchConnectorInfo(@RequestBody String[] bpns, @RequestHeader("Authorization") String bearerToken);

}
