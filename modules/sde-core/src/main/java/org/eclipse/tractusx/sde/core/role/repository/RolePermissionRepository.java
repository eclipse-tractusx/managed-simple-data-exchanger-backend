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

package org.eclipse.tractusx.sde.core.role.repository;

import java.util.List;

import org.eclipse.tractusx.sde.core.role.entity.RolePermissionEntity;
import org.eclipse.tractusx.sde.core.role.entity.RolePermissionMappingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, RolePermissionMappingId> {

	@Query("SELECT p FROM RolePermissionEntity p Where p.sdeRole IN ?1")
	List<RolePermissionEntity> findAll(List<String> sdeRole);

	@Modifying
	@Transactional
	@Query("DELETE FROM RolePermissionEntity p Where p.sdeRole = ?1")
	void deleteRolePermissionMappingBySdeRole(String role);

}
