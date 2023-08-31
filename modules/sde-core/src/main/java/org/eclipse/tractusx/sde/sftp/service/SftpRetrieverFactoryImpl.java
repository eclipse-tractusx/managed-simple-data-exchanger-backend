package org.eclipse.tractusx.sde.sftp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.sde.agent.entity.SftpConfigEntity;
import org.eclipse.tractusx.sde.agent.model.SftpConfigModel;
import org.eclipse.tractusx.sde.agent.repository.FtpsConfigRepository;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.sftp.RetrieverI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.OptionalInt;

@Service
@Profile("SSH")
@RequiredArgsConstructor
public class SftpRetrieverFactoryImpl implements RetrieverFactory {

    @Value("${sftp.host}")
    private String host;
    @Value("${sftp.port:22}")
    private int port;
    @Value("${sftp.username}")
    private String username;
    @Value("${sftp.password}")
    private String password;
    @Value("${sftp.location.tobeprocessed}")
    private String toBeProcessed;
    @Value("${sftp.location.inprogress}")
    private String inProgress;
    @Value("${sftp.location.success}")
    private String success;
    @Value("${sftp.location.partialsucess}")
    private String partialSuccess;
    @Value("${sftp.location.failed}")
    private String failed;

    private final FtpsConfigRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CsvHandlerService csvHandlerService;


    public RetrieverI create(OptionalInt port) throws IOException {
        try {
            var configEntityOptional = repository.findById(SftpConfigEntity.SFTP_CONFIG_ID);
            if (configEntityOptional.isPresent()) {
                SftpConfigModel configModel = objectMapper.convertValue(configEntityOptional.get().getContent(), SftpConfigModel.class);
                return new SftpRetriever(
                        csvHandlerService,
                        configModel.getHost(),
                        port.orElse(configModel.getPort()),
                        configModel.getUsername(),
                        configModel.getPassword(),
                        configModel.getAccessKey(),
                        configModel.getToBeProcessedLocation(),
                        configModel.getInProgressLocation(),
                        configModel.getSuccessLocation(),
                        configModel.getPartialSuccessLocation(),
                        configModel.getFailedLocation()
                );
            } else {
                return new SftpRetriever(
                        csvHandlerService,
                        host,
                        port.orElse(this.port),
                        username,
                        password,
                        null,
                        toBeProcessed,
                        inProgress,
                        success,
                        partialSuccess,
                        failed
                );
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public RetrieverI create() throws IOException {
        return create(OptionalInt.empty());
    }

    @Override
    @Transactional
    public void saveConfig(JsonNode configuration) {
        var configEntityOptional = repository.findById(SftpConfigEntity.SFTP_CONFIG_ID);
        configEntityOptional.ifPresent(configEntity -> configEntity.setContent(configuration.toString()));
        if (configEntityOptional.isEmpty()) {
            SftpConfigEntity configEntity = new SftpConfigEntity();
            configEntity.setUuid(SftpConfigEntity.SFTP_CONFIG_ID);
            configEntity.setContent(configuration.toString());
            configEntity.setType(ConfigType.CLIENT.toString());
            repository.save(configEntity);
        }
    }
}
