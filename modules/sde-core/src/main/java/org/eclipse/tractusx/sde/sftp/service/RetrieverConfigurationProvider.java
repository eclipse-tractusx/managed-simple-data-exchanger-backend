package org.eclipse.tractusx.sde.sftp.service;

import org.eclipse.tractusx.sde.sftp.RetrieverConfiguration;

public interface RetrieverConfigurationProvider {
    void saveRetrieverConfig(RetrieverConfiguration configuration);
    RetrieverConfiguration getRetrieverConfig();
}
