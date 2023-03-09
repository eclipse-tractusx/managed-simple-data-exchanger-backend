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

package org.eclipse.tractusx.sde.submodels.apr.steps;

import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.submodels.apr.model.AspectRelationship;
import org.eclipse.tractusx.sde.submodels.spt.entity.AspectEntity;
import org.eclipse.tractusx.sde.submodels.spt.repository.AspectRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Component
@RequiredArgsConstructor
public class AssemblyPartRelationshipUUIDUrnUUID extends Step {

	private final AspectRepository repository;
	
	@SneakyThrows
	public AspectRelationship run(AspectRelationship input, String processId) {
		
		if (input.getParentUuid() == null || input.getParentUuid().isBlank()) {
			String parentUuid = getUuidIfAspectExists(input.getRowNumber(), input.getParentPartInstanceId(),
					input.getParentManufacturerPartId(), input.getParentOptionalIdentifierKey(),
					input.getParentOptionalIdentifierValue());
			input.setParentUuid(parentUuid);
		}

		if (input.getChildUuid() == null || input.getChildUuid().isBlank()) {
			String childUuid = getUuidIfAspectExists(input.getRowNumber(), input.getChildPartInstanceId(),
					input.getChildManufacturerPartId(), input.getChildOptionalIdentifierKey(),
					input.getChildOptionalIdentifierValue());
			input.setChildUuid(childUuid);
		}

		return input;
	}

	@SneakyThrows
	private String getUuidIfAspectExists(int rowNumber, String partInstanceId, String manufactorerPartId,
			String optionalIdentifierKey, String optionalIdentifierValue) {
		AspectEntity aspect = repository.findByIdentifiers(partInstanceId, manufactorerPartId, optionalIdentifierKey,
				optionalIdentifierValue);

		if (aspect == null) {
			throw new CsvHandlerUseCaseException(rowNumber, String.format(
					"Missing parent aspect for the given Identifier: PartInstanceID: %s | ManufactorerId: %s | %s: %s",
					partInstanceId, manufactorerPartId, optionalIdentifierKey, optionalIdentifierValue));
		}
		return aspect.getUuid();
	}

}
