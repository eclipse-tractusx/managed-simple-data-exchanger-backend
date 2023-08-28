package org.eclipse.tractusx.sde.sftp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.tractusx.sde.sftp.RetrieverConfiguration;

public interface RetrieverConfigurationProvider {
    void saveRetrieverConfig(JsonNode configuration);
    RetrieverConfiguration getRetrieverConfig() throws JsonProcessingException;
}
