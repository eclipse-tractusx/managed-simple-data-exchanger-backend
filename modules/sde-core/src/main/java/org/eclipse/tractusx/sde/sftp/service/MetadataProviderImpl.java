package org.eclipse.tractusx.sde.sftp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.sde.agent.entity.SftpConfigEntity;
import org.eclipse.tractusx.sde.agent.repository.FtpsConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MetadataProviderImpl implements MetadataProvider {

    @Autowired
    private FtpsConfigRepository repository;

    private String metadata = "{\"bpn_numbers\":[\"BPNL00000005PROV\",\"BPNL00000005CONS\",\"TESTVV009\"],\"type_of_access\":\"restricted\",\"usage_policies\":[{\"type\":\"DURATION\",\"typeOfAccess\":\"UNRESTRICTED\",\"value\":\"\",\"durationUnit\":\"SECOND\"},{\"type\":\"ROLE\",\"typeOfAccess\":\"UNRESTRICTED\",\"value\":\"\"},{\"type\":\"PURPOSE\",\"typeOfAccess\":\"UNRESTRICTED\",\"value\":\"\"},{\"type\":\"CUSTOM\",\"typeOfAccess\":\"UNRESTRICTED\",\"value\":\"\"}]}";


    @Override
    public void saveMetadata(JsonNode metadata) {
        List<SftpConfigEntity> entities = repository.findAllByType(ConfigType.METADADA.toString());
        if (!entities.isEmpty()) {
            SftpConfigEntity configEntity = entities.get(0);
            configEntity.setContent(metadata.toString());
            repository.save(configEntity);
        } else {
            SftpConfigEntity configEntity = new SftpConfigEntity();
            configEntity.setUuid(UUID.randomUUID().toString());
            configEntity.setContent(metadata.toString());
            configEntity.setType(ConfigType.METADADA.toString());
            repository.save(configEntity);
        }
    }

    @Override
    public JsonNode getMetadata() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<SftpConfigEntity> entities = repository.findAllByType(ConfigType.METADADA.toString());
            if (entities.isEmpty()) return objectMapper.readTree(metadata);
            else return objectMapper.readTree(entities.get(0).getContent());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
