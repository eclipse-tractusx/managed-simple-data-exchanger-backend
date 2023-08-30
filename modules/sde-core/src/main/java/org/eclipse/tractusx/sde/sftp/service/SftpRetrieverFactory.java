package org.eclipse.tractusx.sde.sftp.service;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.sftp.RetrieverI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Profile("SSH")
@RequiredArgsConstructor
public class SftpRetrieverFactory implements RetrieverFactory{

    private final SshConfigurationProviderImpl sshConfigurationProvider;
    private final CsvHandlerService csvHandlerService;

    @Value("${sftp.port:22}")
    int port;

    @Override
    public RetrieverI create() throws IOException {
        try {
            return new SftpRetriever((SftpRetriever.SshConfiguration) sshConfigurationProvider.getRetrieverConfig(), csvHandlerService, port);
        } catch (JSchException | SftpException e) {
            throw new IOException(e);
        }
    }
}
