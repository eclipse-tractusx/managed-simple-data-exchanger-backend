package org.eclipse.tractusx.sde.sftp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.sde.agent.entity.SftpConfigEntity;
import org.eclipse.tractusx.sde.agent.repository.FtpsConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MetadataProviderImpl implements MetadataProvider {

    @Autowired
    private FtpsConfigRepository repository;

    private String metadata = "{\"bpn_numbers\":[\"BPNL00000005PROV\",\"BPNL00000005CONS\",\"TESTVV009\"],\"type_of_access\":\"restricted\",\"usage_policies\":[{\"type\":\"DURATION\",\"typeOfAccess\":\"UNRESTRICTED\",\"value\":\"\",\"durationUnit\":\"SECOND\"},{\"type\":\"ROLE\",\"typeOfAccess\":\"UNRESTRICTED\",\"value\":\"\"},{\"type\":\"PURPOSE\",\"typeOfAccess\":\"UNRESTRICTED\",\"value\":\"\"},{\"type\":\"CUSTOM\",\"typeOfAccess\":\"UNRESTRICTED\",\"value\":\"\"}]}";


    @Override
    public void saveMetadata(JsonNode metadata) {
        Optional<SftpConfigEntity> config = repository.findById(SftpConfigEntity.METADATA_CONFIG_ID);
        if (config.isPresent()) {
            SftpConfigEntity configEntity = config.get();
            configEntity.setContent(metadata.toString());
            repository.save(configEntity);
        } else {
            SftpConfigEntity configEntity = new SftpConfigEntity();
            configEntity.setUuid(SftpConfigEntity.METADATA_CONFIG_ID);
            configEntity.setContent(metadata.toString());
            configEntity.setType(ConfigType.METADATA.toString());
            repository.save(configEntity);
        }
    }

    @Override
    public JsonNode getMetadata() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Optional<SftpConfigEntity> config = repository.findById(SftpConfigEntity.METADATA_CONFIG_ID);
            if (config.isEmpty()) return objectMapper.readTree(metadata);
            else return objectMapper.readTree(config.get().getContent());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
