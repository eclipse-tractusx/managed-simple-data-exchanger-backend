/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.core.controller.failurelog;

import java.time.LocalDateTime;
import java.util.UUID;

import org.eclipse.tractusx.sde.core.controller.failurelog.entity.FailureLogEntity;
import org.eclipse.tractusx.sde.core.controller.failurelog.repository.FailureLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FailureLogs {

	@Autowired
	private FailureLogRepository repository;

	public void saveLog(String processId, String error) {

		FailureLogEntity entity = FailureLogEntity.builder()
				.uuid(UUID.randomUUID().toString())
				.processId(processId)
				.log(error)
				.dateTime(LocalDateTime.now())
				.build();

		log.error("Error in process {}, {}", entity.getProcessId(), entity.getLog());
		repository.save(entity);
	}
}
