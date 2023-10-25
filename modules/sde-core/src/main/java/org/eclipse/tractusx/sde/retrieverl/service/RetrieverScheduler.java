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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetrieverScheduler {

	private ThreadPoolTaskScheduler taskScheduler;
	private final ProcessRemoteCsv processRemoteCsv;
	private final JobMaintenanceModelProvider jobMaintenanceModelProvider;
	private ScheduledFuture<?> cronFuture = null;

	public void init() {
		taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.initialize();
	}

	public synchronized void schedule(String cronExpression) {
		if (cronFuture != null) {
			cronFuture.cancel(false);
		}
		if (jobMaintenanceModelProvider.getConfiguration().getAutomaticUpload().booleanValue()) {
			init();
			cronFuture = taskScheduler.schedule(
					() -> processRemoteCsv.process(taskScheduler, UUID.randomUUID().toString()),
					new CronTrigger(cronExpression)
			);
			log.info("The Cron Scheduler started successfully as cron expression " + cronExpression);
		} else {
			log.warn("Automatic file upload disable, no new scheduler set for run");
		}
	}

	public String fire() {
		init();
		return processRemoteCsv.process(taskScheduler, UUID.randomUUID().toString());
	}

	public void stopAll() {
		taskScheduler.shutdown();
		cronFuture = null;
	}
}
