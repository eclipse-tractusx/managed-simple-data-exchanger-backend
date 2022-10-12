package com.catenax.dft.api.model.connector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectorInfo {

    String bpn;
    String[] connectorEndpoint;
}
