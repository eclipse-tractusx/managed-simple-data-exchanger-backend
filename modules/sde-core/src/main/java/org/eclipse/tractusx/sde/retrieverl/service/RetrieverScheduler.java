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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;

import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class RetrieverScheduler {

	private final ProcessRemoteCsv processRemoteCsv;
	private final JobMaintenanceConfigService jobMaintenanceConfigService;
	private final SchedulerConfigService schedulerConfigService;
	private ThreadPoolTaskScheduler taskScheduler;

	@Autowired
	public RetrieverScheduler(ProcessRemoteCsv processRemoteCsv, @Lazy JobMaintenanceConfigService jobMaintenanceConfigService, @Lazy SchedulerConfigService schedulerConfigService) {
		this.processRemoteCsv = processRemoteCsv;
		this.jobMaintenanceConfigService = jobMaintenanceConfigService;
		this.schedulerConfigService = schedulerConfigService;
	}

	public synchronized ThreadPoolTaskScheduler reset() {
		Optional.ofNullable(taskScheduler).ifPresent(ThreadPoolTaskScheduler::shutdown);
		taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setWaitForTasksToCompleteOnShutdown(false);
		taskScheduler.initialize();
		return taskScheduler;
	}

	@EventListener(ApplicationReadyEvent.class)
	public synchronized void reschedule() {
		if (jobMaintenanceConfigService.getConfiguration().getAutomaticUpload().booleanValue()) {
			String cronExpression = schedulerConfigService.convertScheduleToCron(schedulerConfigService.getConfiguration());
			reset().schedule(
					() -> processRemoteCsv.process(taskScheduler, UUID.randomUUID().toString()),
					new CronTrigger(cronExpression)
			);
			log.info("The Cron Scheduler started successfully as cron expression " + cronExpression);
		} else {
			log.warn("Automatic file upload disable, no new scheduler set for run");
		}
	}

	public String fire() {
		if (jobMaintenanceConfigService.getConfiguration().getAutomaticUpload().booleanValue()) {
			if (taskScheduler == null || taskScheduler.getScheduledExecutor().isShutdown())
				reset();
			return processRemoteCsv.process(taskScheduler, UUID.randomUUID().toString());
		} else {
			String msg = "Automatic file upload disable, please enable it to trigger job";
			log.warn(msg);
			throw new ValidationException(msg);
		}
	}

	public void stopAll() {
		taskScheduler.shutdown();
	}
}
