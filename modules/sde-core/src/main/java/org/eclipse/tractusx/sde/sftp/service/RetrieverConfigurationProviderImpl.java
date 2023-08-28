package org.eclipse.tractusx.sde.sftp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.sde.agent.entity.SftpConfigEntity;
import org.eclipse.tractusx.sde.agent.model.SftpConfigModel;
import org.eclipse.tractusx.sde.agent.repository.FtpsConfigRepository;
import org.eclipse.tractusx.sde.sftp.RetrieverConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RetrieverConfigurationProviderImpl implements RetrieverConfigurationProvider {

    @Value("${sftp.url}")
    private String serverUrl;
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

    @Autowired
    private FtpsConfigRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void saveRetrieverConfig(JsonNode configuration) {
        List<SftpConfigEntity> entities = repository.findAllByType(ConfigType.CLIENT.toString());
        if (!entities.isEmpty()) {
            SftpConfigEntity configEntity = entities.get(0);
            configEntity.setContent(configuration.toString());
            repository.save(configEntity);
        } else {
            SftpConfigEntity configEntity = new SftpConfigEntity();
            configEntity.setUuid(UUID.randomUUID().toString());
            configEntity.setContent(configuration.toString());
            configEntity.setType(ConfigType.CLIENT.toString());
            repository.save(configEntity);
        }
    }

    @Override
    public RetrieverConfiguration getRetrieverConfig() throws JsonProcessingException {
        List<SftpConfigEntity> entities = repository.findAllByType(ConfigType.CLIENT.toString());
        if (entities.isEmpty()) {
            return new Ftps.FtpConfiguration(serverUrl, username, password, toBeProcessed,
                    inProgress, success, partialSuccess, failed);
        } else {
            JsonNode node = objectMapper.readTree(entities.get(0).getContent());
            SftpConfigModel configModel = objectMapper.convertValue(node, SftpConfigModel.class);
            return new Ftps.FtpConfiguration(configModel.getUrl(), configModel.getUsername(),
                    configModel.getPassword(), configModel.getToBeProcessedLocation(),
                    configModel.getInProgressLocation(), configModel.getSuccessLocation(),
                    configModel.getPartialSuccessLocation(), configModel.getFailedLocation());
        }
    }
}
