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

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
public class RetrieverScheduler {
    private final TaskScheduler taskScheduler = new ConcurrentTaskScheduler();
    private final ProcessRemoteCsv processRemoteCsv;

    private ScheduledFuture<?> cronFuture = null;

    public synchronized void schedule(String cronExpression) {
        if (cronFuture != null) {
            cronFuture.cancel(false);
        }
        cronFuture = taskScheduler.schedule(() -> processRemoteCsv.process(taskScheduler), new CronTrigger(cronExpression));
    }

    public void fire() {
        taskScheduler.schedule(() -> processRemoteCsv.process(taskScheduler), Instant.now());
    }
}
