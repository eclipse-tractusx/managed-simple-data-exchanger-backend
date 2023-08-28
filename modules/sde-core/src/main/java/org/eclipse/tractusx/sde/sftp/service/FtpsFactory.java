package org.eclipse.tractusx.sde.sftp.service;

import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.sftp.RetrieverI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Profile("FTP")
public class FtpsFactory implements RetrieverFactory{

    @Autowired
    private FtpConfigurationProviderImpl ftpConfigurationProvider;
    @Autowired
    private CsvHandlerService csvHandlerService;

    @Override
    public RetrieverI create() throws IOException {
       return new Ftps((Ftps.FtpConfiguration) ftpConfigurationProvider.getRetrieverConfig(), csvHandlerService);
    }
}
