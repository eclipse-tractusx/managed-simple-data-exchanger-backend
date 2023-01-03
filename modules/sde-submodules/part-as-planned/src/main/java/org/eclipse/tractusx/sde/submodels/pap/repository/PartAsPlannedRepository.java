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
package org.eclipse.tractusx.sde.submodels.pap.repository;

import java.util.List;

import org.eclipse.tractusx.sde.submodels.pap.entity.PartAsPlannedEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface PartAsPlannedRepository extends CrudRepository<PartAsPlannedEntity, String> {
	
	PartAsPlannedEntity findByManufacturerPartIdAndNameAtManufacturer(String manufacturerPartId, String nameAtManufacturer);
	
	default PartAsPlannedEntity findByIdentifiers(String manufacturerPartId) {
		return findByManufacturerPartId(manufacturerPartId);
	}

	PartAsPlannedEntity findByUuid(String uuid);

	List<PartAsPlannedEntity> findByProcessId(String processId);
	
	PartAsPlannedEntity findByManufacturerPartId(String manufacturerPartId);

	@Query("select count(pe) from PartAsPlannedEntity pe where pe.updated = ?1 and pe.processId = ?2")
	long countByUpdatedAndProcessId(String updated, String processId);
	
}
