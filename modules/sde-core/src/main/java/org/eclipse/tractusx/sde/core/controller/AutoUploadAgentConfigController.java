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

package org.eclipse.tractusx.sde.core.controller;

import java.util.Map;

import org.eclipse.tractusx.sde.agent.model.ConfigType;
import org.eclipse.tractusx.sde.agent.model.SchedulerConfigModel;
import org.eclipse.tractusx.sde.agent.model.SftpConfigModel;
import org.eclipse.tractusx.sde.sftp.dto.JobMaintenanceModel;
import org.eclipse.tractusx.sde.sftp.service.ConfigService;
import org.eclipse.tractusx.sde.sftp.service.SchedulerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasPermission('','auto_config_management')")
public class AutoUploadAgentConfigController {

	private final ConfigService autoUploadAgentConfigurationService;
	private final SchedulerService schedulerService;

	@PostMapping("/fire")
	public Map<String, String> fire() {
		return schedulerService.fire();
	}

	@PutMapping("/scheduler")
	public JsonNode updateScheduler(@NotBlank @RequestBody SchedulerConfigModel schedulerConfig) {
		JsonNode saveConfiguration = autoUploadAgentConfigurationService.saveConfiguration(ConfigType.SCHEDULER,
				schedulerConfig);
		schedulerService.updateSchedulerExecution(schedulerConfig);
		return saveConfiguration;
	}

	@GetMapping("/scheduler")
	public JsonNode getSchedulerConfig() {
		return autoUploadAgentConfigurationService.getConfiguration(ConfigType.SCHEDULER);
	}

	@PutMapping("/sftp")
	public JsonNode updateSftp(@NotBlank @RequestBody SftpConfigModel config) {
		return autoUploadAgentConfigurationService.saveConfiguration(ConfigType.SFTP, config);
	}

	@GetMapping("/sftp")
	public JsonNode getSftpConfig() {
		return autoUploadAgentConfigurationService.getConfiguration(ConfigType.SFTP);
	}

	@PutMapping("/notification")
	public JsonNode updateNotification(@NotBlank @RequestBody JsonNode config) {
		return autoUploadAgentConfigurationService.saveConfiguration(ConfigType.NOTIFICATION, config);
	}

	@GetMapping("/notification")
	public JsonNode getNotificationConfig() {
		return autoUploadAgentConfigurationService.getConfiguration(ConfigType.NOTIFICATION);
	}

	@PutMapping("/job-maintenance")
	public JsonNode updateJobMaintenance(@NotBlank @RequestBody JobMaintenanceModel config) {
		JsonNode saveConfiguration = autoUploadAgentConfigurationService.saveConfiguration(ConfigType.JOB_MAINTENANCE,
				config);
		schedulerService.updateScehdulreStatus(config);
		return saveConfiguration;
	}

	@GetMapping("/job-maintenance")
	public JsonNode getJobMaintenanceConfig() {
		return autoUploadAgentConfigurationService.getConfiguration(ConfigType.JOB_MAINTENANCE);
	}

}
