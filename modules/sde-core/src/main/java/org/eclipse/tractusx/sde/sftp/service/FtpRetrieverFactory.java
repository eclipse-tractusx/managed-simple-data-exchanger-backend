package org.eclipse.tractusx.sde.sftp.service;

import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.sftp.RetrieverI;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Profile("FTP")
@RequiredArgsConstructor
public class FtpRetrieverFactory implements RetrieverFactory{

    private final FtpConfigurationProviderImpl ftpConfigurationProvider;
    private final CsvHandlerService csvHandlerService;

    @Override
    public RetrieverI create() throws IOException {
       return new FtpRetriever((FtpRetriever.FtpConfiguration) ftpConfigurationProvider.getRetrieverConfig(), csvHandlerService);
    }
}
