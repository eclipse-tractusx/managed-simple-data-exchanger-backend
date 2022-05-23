package com.catenax.dft.gateways.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EDCAssetGateway extends EDCGateway {

    @Value(value = "${edc.aspect.url}")
    private String edcAspectEndpoint;
    @Value(value = "${edc.aspect.apiKey}")
    private String aspectApiKey;
    @Value(value = "${edc.aspect.apiValue}")
    private String aspectApiValue;

    @Override
    protected String getEndPoint() {
        return edcAspectEndpoint;
    }

    @Override
    protected String getApiKey() {
        return aspectApiKey;
    }

    @Override
    protected String getApiValue() {
        return aspectApiValue;
    }

}
