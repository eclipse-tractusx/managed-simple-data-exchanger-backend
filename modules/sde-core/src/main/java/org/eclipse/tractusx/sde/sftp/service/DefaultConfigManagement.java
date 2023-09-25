/********************************************************************************
 * Copyright (c) 2023 T-Systems International GmbH
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.tractusx.sde.sftp.service;

import java.util.List;
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
import net.minidev.json.JSONObject;

@Component
@RequiredArgsConstructor
public class DefaultConfigManagement {

	private final AutoUploadAgentConfigRepository configRepository;
	private final ConfigService configService;
	private final SchedulerService schedulerService;
	private final PolicyProvider policyProvider;
	private final  SftpRetrieverFactoryImpl sftpRetrieverFactoryImpl;
	ObjectMapper mapper = new ObjectMapper();

	@Value("${mail.to.address}")
	private String toEmail;

	@Value("${mail.cc.address}")
	private String ccEmail;

	@SneakyThrows
	@EventListener(ApplicationReadyEvent.class)
	public void saveDefaultConfiguration() {
		policyProvider.saveDefaultPolicy();
		saveJobMaintanceConfiguration();
		saveDefaultNotificationConfiguration();
		sftpRetrieverFactoryImpl.saveDefaultConfig();
		schedulerService.saveDefaultScheduler();
	}

	@SneakyThrows
	private void saveDefaultNotificationConfiguration() {
		Optional<ConfigEntity> config = configRepository.findAllByType(ConfigType.NOTIFICATION.toString());
		if (config.isEmpty()) {
			configService.saveConfiguration(ConfigType.NOTIFICATION, getJsonNotificationBody());
		}

	}

	@SneakyThrows
	private void saveJobMaintanceConfiguration() {
		Optional<ConfigEntity> config = configRepository.findAllByType(ConfigType.JOB_MAINTENANCE.toString());
		if (config.isEmpty()) {
			configService.saveConfiguration(ConfigType.JOB_MAINTENANCE, getJsonJobMaiantainceBody());
		}

	}

	private JSONObject getJsonNotificationBody() {
		JSONObject json = new JSONObject();
		json.put("to_email", List.of(toEmail));
		json.put("cc_email", List.of(ccEmail));
		return json;
	}

	private JSONObject getJsonJobMaiantainceBody() {
		JSONObject json = new JSONObject();
		json.put("automatic_upload", true);
		json.put("email_notification", true);
		return json;
	}
}
