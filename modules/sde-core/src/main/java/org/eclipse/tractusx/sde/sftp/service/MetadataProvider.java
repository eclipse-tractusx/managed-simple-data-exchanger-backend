package org.eclipse.tractusx.sde.sftp.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface MetadataProvider {
    void saveMetadata(JsonNode metadata);
    JsonNode getMetadata();
}
