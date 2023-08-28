package org.eclipse.tractusx.sde.sftp.config;

import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.sftp.service.FtpConfigurationProviderImpl;
import org.eclipse.tractusx.sde.sftp.service.Ftps;
import org.eclipse.tractusx.sde.sftp.service.RetrieverConfigurationProvider;
import org.eclipse.tractusx.sde.sftp.service.RetrieverFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class AgentConfigurer {

    @Autowired
    CsvHandlerService csvHandlerService;
    @Bean
    public RetrieverFactory getFtpRetriever(@Autowired RetrieverConfigurationProvider configurationProvider) {
        Optional<RetrieverFactory> retrieverFactory = Optional.empty();
        if (configurationProvider instanceof FtpConfigurationProviderImpl ftpConfigurationProvider) {
            retrieverFactory = Optional.of( () -> new Ftps((Ftps.FtpConfiguration) ftpConfigurationProvider.getRetrieverConfig(), csvHandlerService) );
        }
        return retrieverFactory.orElseThrow();
    }

}
