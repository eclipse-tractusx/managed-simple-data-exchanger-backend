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

package org.eclipse.tractusx.sde.submodels.batch.steps;

import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.submodels.batch.entity.BatchEntity;
import org.eclipse.tractusx.sde.submodels.batch.mapper.BatchMapper;
import org.eclipse.tractusx.sde.submodels.batch.model.Batch;
import org.eclipse.tractusx.sde.submodels.batch.repository.BatchRepository;
import org.springframework.stereotype.Service;

@Service
public class StoreBatchCsvHandlerUseCase extends Step {

	private final BatchRepository batchRepository;
	private final BatchMapper batchMapper;

	public StoreBatchCsvHandlerUseCase(BatchRepository batchRepository, BatchMapper mapper) {
		this.batchRepository = batchRepository;
		this.batchMapper = mapper;
	}

	public Batch run(Batch input) {
		BatchEntity entity = batchMapper.mapFrom(input);
		batchRepository.save(entity);

		return input;
	}
}