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

import java.util.Map;
import java.util.Optional;

import org.eclipse.tractusx.sde.agent.entity.ConfigEntity;
import org.eclipse.tractusx.sde.agent.model.ConfigType;
import org.eclipse.tractusx.sde.agent.model.SchedulerConfigModel;
import org.eclipse.tractusx.sde.agent.model.SchedulerType;
import org.eclipse.tractusx.sde.agent.repository.AutoUploadAgentConfigRepository;
import org.eclipse.tractusx.sde.sftp.dto.JobMaintenanceModel;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class SchedulerService {

	private final AutoUploadAgentConfigRepository configRepository;
	private final ConfigService configService;
	private final RetrieverScheduler retrieverScheduler;

	private final ObjectMapper mapper = new ObjectMapper();

	public Map<String,String> fire() {
		return Map.of("msg", retrieverScheduler.fire());
	}
	
	@SneakyThrows
	public void saveDefaultScheduler() {
		Optional<ConfigEntity> config = configRepository.findAllByType(ConfigType.SCHEDULER.toString());
		if (config.isEmpty()) {
			SchedulerConfigModel schedulerConfigModel = SchedulerConfigModel.builder().type(SchedulerType.HOURLY)
					.time("1").build();

			configService.saveConfiguration(ConfigType.SCHEDULER, schedulerConfigModel);

			// Start the scheduler
			updateSchedulerExecution(schedulerConfigModel);
		} else {
			// start scehduler
			updateSchedulerExecution(mapper.readValue(config.get().getContent(), SchedulerConfigModel.class));
		}
	}

	public void updateSchedulerExecution(SchedulerConfigModel model) {
		// update the scheduler
		retrieverScheduler.schedule(convertScheduleToCron(model));
	}

	public void updateScehdulreStatus(JobMaintenanceModel config) {
		if (config.isAutomaticUpload()) {
			// enable the scheduler
			enable();
		} else {
			// disable the scheduler
			retrieverScheduler.stopAll();
		}
	}
	
	
	private void enable() {
		SchedulerConfigModel config = configService.getSchedulerDetails();
		retrieverScheduler.schedule(convertScheduleToCron(config));
	}

	public String convertScheduleToCron(SchedulerConfigModel model) {
		switch (model.getType()) {
		case DAILY -> {
			String time = model.getTime();
			//21:00
			String[] timeArr = time.split(":");
			String hour = "00";
			String minute = "00";

			if (timeArr.length == 2) {
				hour = timeArr[0];
				minute = timeArr[1];
			}
			return "0 " + minute + " " + hour + " * * *";
		}
		case HOURLY -> {
			 return "0 0 0/" + model.getTime() + " * * *";
		}
		case WEEKLY -> {
			String time = model.getTime();
			String[] timeArr = time.split(":");
			String hour = "00";
			String minute = "00";

			if (timeArr.length == 2) {
				hour = timeArr[0];
				minute = timeArr[1];
			}
			return "0 " + minute + " " + hour + " * * " + model.getDay();
		}
		default -> {
			return "0 0 0/1 * * *";
		}
		}
	}
}
