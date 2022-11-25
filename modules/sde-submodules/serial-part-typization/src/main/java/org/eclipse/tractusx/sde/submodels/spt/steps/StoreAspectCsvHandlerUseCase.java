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

package org.eclipse.tractusx.sde.submodels.spt.steps;

import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.submodels.spt.entity.AspectEntity;
import org.eclipse.tractusx.sde.submodels.spt.mapper.AspectMapper;
import org.eclipse.tractusx.sde.submodels.spt.model.Aspect;
import org.eclipse.tractusx.sde.submodels.spt.repository.AspectRepository;
import org.springframework.stereotype.Service;

@Service
public class StoreAspectCsvHandlerUseCase extends Step {

	private final AspectRepository aspectRepository;
	private final AspectMapper aspectMapper;

	public StoreAspectCsvHandlerUseCase(AspectRepository aspectRepository, AspectMapper mapper) {
		this.aspectRepository = aspectRepository;
		this.aspectMapper = mapper;
	}

	public Aspect run(Aspect input) {
		AspectEntity entity = aspectMapper.mapFrom(input);
		aspectRepository.save(entity);
		return input;
	}
}