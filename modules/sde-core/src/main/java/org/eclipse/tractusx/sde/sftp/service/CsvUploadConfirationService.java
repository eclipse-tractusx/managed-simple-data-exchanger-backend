package org.eclipse.tractusx.sde.sftp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.sde.agent.entity.CsvUploadConfigEntity;
import org.eclipse.tractusx.sde.agent.repository.CsvUploadConfigRepository;
import org.eclipse.tractusx.sde.sftp.dto.EmailNotificationModel;
import org.eclipse.tractusx.sde.sftp.dto.JobMaintenanceModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CsvUploadConfirationService {

    private final CsvUploadConfigRepository repository;
    private final ObjectMapper mapper = new ObjectMapper();

    /* We are using this method to save any type of CsvUpload config using concrete type of config that we need */
    public String saveCsvUploadConfig(JsonNode config, ConfigType configType) throws JsonProcessingException {
        List<CsvUploadConfigEntity> notificationList = repository.findAllByType(configType.toString());
        if (notificationList.isEmpty()) {
            CsvUploadConfigEntity configEntity = new CsvUploadConfigEntity();
            configEntity.setUuid(UUID.randomUUID().toString());
            configEntity.setType(configType.toString());
            configEntity.setContent(mapper.writeValueAsString(config));
            repository.save(configEntity);
            return configEntity.getUuid();
        } else {
            CsvUploadConfigEntity configEntity = notificationList.get(0);
            configEntity.setContent(mapper.writeValueAsString(config));
            repository.save(configEntity);
            return configEntity.getUuid();
        }

    }


    public Object getCsvUploadConfig(ConfigType configType) {
        List<CsvUploadConfigEntity> notificationList = repository.findAllByType(configType.toString());
        if (notificationList.isEmpty()) return null;
        else {
            CsvUploadConfigEntity configEntity = notificationList.get(0);
            switch (configType) {
                case NOTIFICATION -> {
                    return  mapper.convertValue(configEntity.getContent(), EmailNotificationModel.class);
                }
                case JOB_MAINTENANCE -> {
                    return mapper.convertValue(configEntity.getContent(), JobMaintenanceModel.class);
                }
            }
        }
        return null;
    }
}
