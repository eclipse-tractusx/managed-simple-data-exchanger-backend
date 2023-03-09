/********************************************************************************
 * Copyright (c) 2022, 2023 T-Systems International GmbH
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.sde.submodels.psiap.steps;

import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.submodels.psiap.entity.PartSiteInformationAsPlannedEntity;
import org.eclipse.tractusx.sde.submodels.psiap.mapper.PartSiteInformationAsPlannedMapper;
import org.eclipse.tractusx.sde.submodels.psiap.model.PartSiteInformationAsPlanned;
import org.eclipse.tractusx.sde.submodels.psiap.repository.PartSiteInformationAsPlannedRepository;
import org.springframework.stereotype.Service;

@Service
public class StorePartSiteInformationAsPlannedHandlerStep extends Step {

	private final PartSiteInformationAsPlannedRepository partSiteInformationAsPlannedRepository;
	private final PartSiteInformationAsPlannedMapper partSiteInformationAsPlannedMapper;

	public StorePartSiteInformationAsPlannedHandlerStep(PartSiteInformationAsPlannedRepository partSiteInformationAsPlannedRepository, PartSiteInformationAsPlannedMapper mapper) {
		this.partSiteInformationAsPlannedRepository = partSiteInformationAsPlannedRepository;
		this.partSiteInformationAsPlannedMapper = mapper;
	}

	public PartSiteInformationAsPlanned run(PartSiteInformationAsPlanned input) {
		PartSiteInformationAsPlannedEntity entity = partSiteInformationAsPlannedMapper.mapFrom(input);
		partSiteInformationAsPlannedRepository.save(entity);
		return input;
	}
}