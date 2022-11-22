/********************************************************************************
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
package org.eclipse.tractusx.sde.submodels.pap.steps;

import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.submodels.pap.entity.PartAsPlannedEntity;
import org.eclipse.tractusx.sde.submodels.pap.mapper.PartAsPlannedMapper;
import org.eclipse.tractusx.sde.submodels.pap.model.PartAsPlanned;
import org.eclipse.tractusx.sde.submodels.pap.repository.PartAsPlannedRepository;
import org.springframework.stereotype.Service;

@Service
public class StorePartAsPlannedHandlerStep extends Step {

	private final PartAsPlannedRepository partAsPlannedRepository;
	private final PartAsPlannedMapper partAsPlannedMapper;

	public StorePartAsPlannedHandlerStep(PartAsPlannedRepository partAsPlannedRepository, PartAsPlannedMapper mapper) {
		this.partAsPlannedRepository = partAsPlannedRepository;
		this.partAsPlannedMapper = mapper;
	}

	public PartAsPlanned run(PartAsPlanned input) {
		PartAsPlannedEntity entity = partAsPlannedMapper.mapFrom(input);
		partAsPlannedRepository.save(entity);
		return input;
	}
}