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

package org.eclipse.tractusx.sde.retrieverl.service;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.agent.ConfigService;
import org.eclipse.tractusx.sde.agent.model.SchedulerConfigModel;
import org.eclipse.tractusx.sde.agent.model.SchedulerType;
import org.eclipse.tractusx.sde.common.ConfigurationProvider;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("scheduler")
public class SchedulerConfigService implements ConfigurationProvider<SchedulerConfigModel> {

	private final ConfigService configService;
	private final RetrieverScheduler retrieverScheduler;

	public SchedulerConfigService(ConfigService configService, @Lazy RetrieverScheduler retrieverScheduler) {
		this.configService = configService;
		this.retrieverScheduler = retrieverScheduler;
	}

	public String convertScheduleToCron(SchedulerConfigModel model) {
		switch (model.getType()) {
		case DAILY -> {
			int[] timeArr = timeValidate(model);
			return "0 " + timeArr[1] + " " + timeArr[0] + " * * *";
		}

		case HOURLY -> {
			timeHourValidation(model);
			return "0 0 0/" + model.getTime() + " * * *";
		}
		case WEEKLY -> {
			int[] timeArr = timeValidate(model);
			dayValidation(model);
			return "0 " + timeArr[1] + " " + timeArr[0] + " * * " + model.getDay();
		}
		default -> {
			return "0 0 0/1 * * *";
		}
		}
	}

	private int[] timeValidate(SchedulerConfigModel model) {
		// 21:00
		String time = model.getTime();
		if (StringUtils.isBlank(time))
			throw new ValidationException(
					"Time should not be null or empty, it should be like 24 hours e.g 21:00(hour:minute) or like 03:30 AM");
		time = time.toUpperCase();
		if (time.contains("AM") || time.contains("PM")) {
			try {
				time = LocalTime.parse(time.toUpperCase(), DateTimeFormatter.ofPattern("hh:mm a", Locale.GERMANY))
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
						+ "' time is not in correct format, it should be like 24 hours e.g 21:00(hour:minute) or like 03:30 AM");
		}
		String[] split = time.split(":");
 		int[] intsplit = new int[2];
 		int i = 0;
 		for (String string : split) {
 			intsplit[i++] = Integer.parseInt(string);
 		}
 		return intsplit;
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

		// 24 hour number time
		String regex = "(0?[1-9]|1[0-9]|2[0-4])";
		String time = model.getTime();
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(time);
		if (StringUtils.isBlank(time))
			throw new ValidationException("Time should not be null or empty, it should be number");

		if (!m.matches())
			throw new ValidationException("'" + time + "' time is not number between [1-24]");
	}


	@Override
	public SchedulerConfigModel getConfiguration() {
		return configService.getConfigurationAsObject(SchedulerConfigModel.class)
				.orElseGet(() -> {
					var scm = getDefaultSchedulerConfigModel();
					configService.saveConfiguration(scm);
					return scm;
				});
	}

	@Override
	public void saveConfig(SchedulerConfigModel config) {
		configService.saveConfiguration(config);
		retrieverScheduler.reschedule();
	}

	@Override
	public Class<SchedulerConfigModel> getConfigClass() {
		return SchedulerConfigModel.class;
	}

	private SchedulerConfigModel getDefaultSchedulerConfigModel() {
		return SchedulerConfigModel.builder()
				.type(SchedulerType.DAILY)
				.time("01:00")
				.build();
	}

}
