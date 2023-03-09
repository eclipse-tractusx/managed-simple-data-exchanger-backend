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

package org.eclipse.tractusx.sde.submodels.sluab.steps;

import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.submodels.sluab.entity.SingleLevelUsageAsBuiltEntity;
import org.eclipse.tractusx.sde.submodels.sluab.mapper.SingleLevelUsageAsBuiltMapper;
import org.eclipse.tractusx.sde.submodels.sluab.model.SingleLevelUsageAsBuilt;
import org.eclipse.tractusx.sde.submodels.sluab.repository.SingleLevelUsageAsBuiltRepository;
import org.springframework.stereotype.Service;

@Service
public class StoreSingleLevelUsageAsBuiltCsvHandlerUseCase extends Step {

	private final SingleLevelUsageAsBuiltRepository repository;
	private final SingleLevelUsageAsBuiltMapper mapper;

	public StoreSingleLevelUsageAsBuiltCsvHandlerUseCase(SingleLevelUsageAsBuiltRepository aspectRelationshipRepository,
			SingleLevelUsageAsBuiltMapper mapper) {
		this.repository = aspectRelationshipRepository;
		this.mapper = mapper;
	}

	public SingleLevelUsageAsBuilt run(SingleLevelUsageAsBuilt input) {
		SingleLevelUsageAsBuiltEntity entity = mapper.mapFrom(input);
		repository.save(entity);
		return input;
	}
}