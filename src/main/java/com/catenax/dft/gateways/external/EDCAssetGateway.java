package com.catenax.dft.gateways.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EDCAssetGateway extends EDCGateway {

    @Value(value = "${edc.aspect.url}")
    private String edcEndpoint;
    @Value(value = "${edc.aspect.apiKey}")
    private String apiKey;
    @Value(value = "${edc.aspect.apiValue}")
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
