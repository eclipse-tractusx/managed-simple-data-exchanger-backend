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
package org.eclipse.tractusx.sde.submodels.sluab.repository;

import java.util.List;

import org.eclipse.tractusx.sde.submodels.sluab.entity.SingleLevelUsageAsBuiltEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SingleLevelUsageAsBuiltRepository extends JpaRepository<SingleLevelUsageAsBuiltEntity, String> {

	List<SingleLevelUsageAsBuiltEntity> findByParentCatenaXId(String parentCatenaXId);
	
	SingleLevelUsageAsBuiltEntity findByChildCatenaXId(String uuid);

	List<SingleLevelUsageAsBuiltEntity> findByProcessId(String processId);

	@Query("select count(ar) from SingleLevelUsageAsBuiltEntity ar where ar.updated = ?1 and ar.processId = ?2")
	long countByUpdatedAndProcessId(String updated, String processId);

}
