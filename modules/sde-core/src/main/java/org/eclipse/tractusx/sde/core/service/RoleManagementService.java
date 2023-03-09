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

package org.eclipse.tractusx.sde.core.service;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.core.role.entity.RolePermissionEntity;
import org.eclipse.tractusx.sde.core.role.repository.RolePermissionCustomRepository;
import org.eclipse.tractusx.sde.core.role.repository.RolePermissionRepository;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Service
@AllArgsConstructor
public class RoleManagementService {

	private final RolePermissionRepository rolePermissionRepository;
	private final RolePermissionCustomRepository rolePermissionCustomRepository;

	public String saveRoleWithPermission(String role, List<String> rolemappping) {
		List<RolePermissionEntity> allentity = rolemappping.stream()
				.map(e -> RolePermissionEntity.builder().sdePermission(e).sdeRole(role).build()).toList();
		rolePermissionRepository.deleteRolePermissionMappingBySdeRole(role);
		rolePermissionRepository.saveAll(allentity);
		return "Role Permission saved successfully";
	}

	@SneakyThrows
	public List<RolePermissionEntity> findAll(List<String> role, List<String> permission) {
		return rolePermissionCustomRepository.findAll(role, permission);
	}

	@SneakyThrows
	public List<String> getRolePermission(List<String> role) {
		List<RolePermissionEntity> allPermission = Optional.ofNullable(rolePermissionRepository.findAll(role))
				.orElseThrow(() -> new NoDataFoundException(role + " Role not found to update permission"));
		return allPermission.stream().map(RolePermissionEntity::getSdePermission).toList();
	}
}
