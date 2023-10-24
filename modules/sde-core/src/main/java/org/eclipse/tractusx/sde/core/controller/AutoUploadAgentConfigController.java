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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.sde.agent.ConfigService;
import org.eclipse.tractusx.sde.agent.model.EmailNotificationModel;
import org.eclipse.tractusx.sde.agent.model.JobMaintenanceModel;
import org.eclipse.tractusx.sde.agent.model.MinioConfigModel;
import org.eclipse.tractusx.sde.agent.model.SchedulerConfigModel;
import org.eclipse.tractusx.sde.agent.model.SftpConfigModel;
import org.eclipse.tractusx.sde.notification.manager.EmailNotificationModelProvider;
import org.eclipse.tractusx.sde.sftp.service.JobMaintenanceModelProvider;
import org.eclipse.tractusx.sde.sftp.service.MinioRetrieverFactoryImpl;
import org.eclipse.tractusx.sde.sftp.service.SchedulerService;
import org.eclipse.tractusx.sde.sftp.service.SftpRetrieverFactoryImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@PreAuthorize("hasPermission('','auto_config_management')")
@RequiredArgsConstructor
public class AutoUploadAgentConfigController {

	private final ConfigService configService;
	private final SchedulerService schedulerService;
	private final MinioRetrieverFactoryImpl minioRetrieverFactory;
	private final SftpRetrieverFactoryImpl sftpRetrieverFactory;
	private final EmailNotificationModelProvider emailNotificationModelProvider;
	private final JobMaintenanceModelProvider jobMaintenanceModelProvider;


	@PostMapping("/fire")
	public Map<String, String> fire() {
		return schedulerService.fire();
	}

	@PutMapping("/scheduler")
	public void updateScheduler(@RequestBody @Valid SchedulerConfigModel schedulerConfigModel) {
		schedulerService.saveConfig(schedulerConfigModel);
		schedulerService.updateSchedulerExecution(schedulerConfigModel);
	}

	@GetMapping("/scheduler")
	public SchedulerConfigModel getSchedulerConfig() {
		return schedulerService.getConfiguration();
	}

	@PutMapping("/sftp")
	public void updateSftp(@RequestBody @Valid SftpConfigModel sftpConfigModel) {
		sftpRetrieverFactory.saveConfig(sftpConfigModel);
	}

	@PutMapping("/minio")
	public void updateMinio(@RequestBody @Valid MinioConfigModel minioConfigModel) {
		minioRetrieverFactory.saveConfig(minioConfigModel);
	}

	@GetMapping("/minio")
	public MinioConfigModel getMinioConfig() {
		return minioRetrieverFactory.getConfiguration();
	}

	@GetMapping("/sftp")
	public SftpConfigModel getSftpConfig() {
		return sftpRetrieverFactory.getConfiguration();
	}

	@PutMapping("/notification")
	public void updateNotification(@RequestBody @Valid EmailNotificationModel emailNotificationModel) {
		emailNotificationModelProvider.saveConfig(emailNotificationModel);
	}

	@GetMapping("/notification")
	public EmailNotificationModel getNotificationConfig() {
		return emailNotificationModelProvider.getConfiguration();
	}

	@PutMapping("/job-maintenance")
	public void updateJobMaintenance(@RequestBody @Valid JobMaintenanceModel jobMaintenanceModel) {
		jobMaintenanceModelProvider.saveConfig(jobMaintenanceModel);
		schedulerService.updateScheduleStatus(jobMaintenanceModel);
	}

	@GetMapping("/job-maintenance")
	public JobMaintenanceModel getJobMaintenanceConfig() {
		return jobMaintenanceModelProvider.getConfiguration();
	}

}
