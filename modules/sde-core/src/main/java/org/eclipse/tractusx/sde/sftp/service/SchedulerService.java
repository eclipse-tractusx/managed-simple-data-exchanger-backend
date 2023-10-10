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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.agent.entity.ConfigEntity;
import org.eclipse.tractusx.sde.agent.model.ConfigType;
import org.eclipse.tractusx.sde.agent.model.SchedulerConfigModel;
import org.eclipse.tractusx.sde.agent.model.SchedulerType;
import org.eclipse.tractusx.sde.agent.repository.AutoUploadAgentConfigRepository;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
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

	public Map<String, String> fire() {
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
			String[] timeArr = timeValidate(model);
			return "0 " + timeArr[1] + " " + timeArr[0] + " * * *";
		}

		case HOURLY -> {
			timeHourValidation(model);
			return "0 0 0/" + model.getTime() + " * * *";
		}
		case WEEKLY -> {
			String[] timeArr = timeValidate(model);
			dayValidation(model);
			return "0 " + timeArr[1] + " " + timeArr[0] + " * * " + model.getDay();
		}
		default -> {
			return "0 0 0/1 * * *";
		}
		}
	}

	private String[] timeValidate(SchedulerConfigModel model) {
		// 21:00
		String time = model.getTime();
		if (StringUtils.isBlank(time))
			throw new ValidationException(
					"Time should not be null or empty, it should be like 24 hours 21:00(hour:minute) or like 03:30 AM");
		time = time.toUpperCase();
		if (time.contains("AM") || time.contains("PM")) {
			try {
				time = LocalTime.parse(time.toUpperCase(), DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
						.format(DateTimeFormatter.ofPattern("HH:mm"));
			} catch (Exception e) {
				throw new ValidationException(e.getMessage());
			}
		} else {
			String regex24 = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
			Pattern p1 = Pattern.compile(regex24);
			Matcher m1 = p1.matcher(time);
			if (!m1.matches())
				throw new ValidationException("'" + time
						+ "' time is not in correct format, it should be like 24 hours 21:00(hour:minute) or like 03:30 AM");
		}

		return time.split(":");
	}

	private void dayValidation(SchedulerConfigModel model) {

		// 1-7 number day
		String regex = "[0-6]";
		String day = model.getDay();
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(day);
		if (StringUtils.isBlank(day))
			throw new ValidationException("Day should not be null or empty, it should be number");

		if (!m.matches())
			throw new ValidationException("'" + day + "' day is not number between [0-6]");
	}

	private void timeHourValidation(SchedulerConfigModel model) {

		// 1-7 number day
		String regex = "([1]?[1-9]|2[1-4])";
		String time = model.getTime();
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(time);
		if (StringUtils.isBlank(time))
			throw new ValidationException("Time should not be null or empty, it should be number");

		if (!m.matches())
			throw new ValidationException("'" + time + "' time is not number between [1-24]");
	}
}
