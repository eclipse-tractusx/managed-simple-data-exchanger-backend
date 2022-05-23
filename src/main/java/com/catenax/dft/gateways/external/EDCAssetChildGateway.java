package com.catenax.dft.gateways.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EDCAssetChildGateway extends EDCGateway {

    @Value(value = "${edc.child.aspect.url}")
    private String edcEndpoint;
    @Value(value = "${edc.child.aspect.apiKey}")
    private String apiKey;
    @Value(value = "${edc.child.aspect.apiValue}")
    private String apiValue;

    @Override
    protected String getEndPoint() {
        return edcEndpoint;
    }

    @Override
    protected String getApiKey() {
        return apiKey;
    }

    @Override
    protected String getApiValue() {
        return apiValue;
    }
}
