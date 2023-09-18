package org.eclipse.tractusx.sde.sftp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.sde.agent.entity.CsvUploadConfigEntity;
import org.eclipse.tractusx.sde.agent.repository.CsvUploadConfigRepository;
import org.eclipse.tractusx.sde.sftp.dto.SchedulerModel;
import org.eclipse.tractusx.sde.sftp.dto.SchedulerType;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final CsvUploadConfigRepository repository;
    private final RetrieverScheduler retrieverScheduler;
    private final ObjectMapper mapper = new ObjectMapper();


    @EventListener(ApplicationReadyEvent.class)
    public void saveDefaultScheduler() throws JsonProcessingException {
        if (repository.findAll().isEmpty()) {
            SchedulerModel model = new SchedulerModel();
            model.setType(SchedulerType.HOURLY);
            model.setTime("1");

            CsvUploadConfigEntity configEntity = new CsvUploadConfigEntity();
            configEntity.setUuid(UUID.randomUUID().toString());
            configEntity.setType(ConfigType.SCHEDULER.toString());
            configEntity.setContent(mapper.writeValueAsString(model));

            repository.save(configEntity);
        }
    }

    public String updateScheduler(String uuid, JsonNode schedulerConfig) throws JsonProcessingException {
        Optional<CsvUploadConfigEntity> optional = repository.findById(uuid);
        if (optional.isPresent()) {
            CsvUploadConfigEntity configEntity = optional.get();
            configEntity.setContent(mapper.writeValueAsString(schedulerConfig));
            repository.save(configEntity);
            return configEntity.getUuid();
        } else return null;
    }


    public String getCurrentSchedule() {
        CsvUploadConfigEntity configEntity = repository.findAllByType(ConfigType.SCHEDULER.toString()).get(0);
        SchedulerModel model = mapper.convertValue(configEntity.getContent(), SchedulerModel.class);
        switch (model.getType()) {
            case DAILY -> {
                return "0 0 " + model.getTime() + " * * *";
            }
            case HOURLY -> {
                return "0 */ " + model.getTime() + " * * * *";
            }
            case WEEKLY -> {
                return "0 " + model.getTime() + " * * *" + model.getDay();
            }
            default -> {
                return "0 0 * * * *";
            }
        }
    }
}
