package org.eclipse.tractusx.sde.sftp.service;

import java.util.Optional;

import org.eclipse.tractusx.sde.agent.entity.ConfigEntity;
import org.eclipse.tractusx.sde.agent.model.ConfigType;
import org.eclipse.tractusx.sde.agent.repository.AutoUploadAgentConfigRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoUploadAgentDefaultConfigManagement {

	private final AutoUploadAgentConfigRepository configRepository;
	private final SchedulerService schedulerService;
	private final PolicyProvider policyProvider;
	ObjectMapper mapper = new ObjectMapper();

	@Value("${mail.to.address}")
	private String toEmail;

	@Value("${mail.cc.address}")
	private String ccEmail;

	@SneakyThrows
	@EventListener(ApplicationReadyEvent.class)
	public void saveDefaultConfiguration() {
		schedulerService.saveDefaultScheduler();
		policyProvider.saveDefaultPolicy();
		saveDefaultNotificationConfiguration();
		saveJobMaintanceConfiguration();
	}

	@SneakyThrows
	private void saveDefaultNotificationConfiguration() {
		Optional<ConfigEntity> config = configRepository.findAllByType(ConfigType.NOTIFICATION.toString());
		if (config.isEmpty()) {
			ConfigEntity configEntity = new ConfigEntity();
			configEntity.setType(ConfigType.NOTIFICATION.toString());
			configEntity.setContent(mapper.writeValueAsString(getJsonNotificationBody()));
			configRepository.save(configEntity);
			log.info("The notification setting save in successfully");
		}

	}

	@SneakyThrows
	private void saveJobMaintanceConfiguration() {
		Optional<ConfigEntity> config = configRepository.findAllByType(ConfigType.JOB_MAINTENANCE.toString());
		if (config.isEmpty()) {
			ConfigEntity configEntity = new ConfigEntity();
			configEntity.setType(ConfigType.JOB_MAINTENANCE.toString());
			configEntity.setContent(mapper.writeValueAsString(getJsonJobMaiantainceBody()));
			configRepository.save(configEntity);
			log.info("The job maintance setting save in successfully");
		}

	}

	private JSONObject getJsonNotificationBody() {
		JSONObject json = new JSONObject();
		json.put("to_email", toEmail);
		json.put("cc_email", ccEmail);
		return json;
	}

	private JSONObject getJsonJobMaiantainceBody() {
		JSONObject json = new JSONObject();
		json.put("automatic_upload", "enable");
		json.put("email_notification", "enable");
		return json;
	}
}
