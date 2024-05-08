/********************************************************************************
 * Copyright (c) 2022, 2024 T-Systems International GmbH
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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
import java.util.Map;

import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.core.role.entity.RoleEntity;
import org.eclipse.tractusx.sde.core.role.entity.RolePermissionEntity;
import org.eclipse.tractusx.sde.core.role.entity.RolePojo;
import org.eclipse.tractusx.sde.core.role.repository.RolePermissionCustomRepository;
import org.eclipse.tractusx.sde.core.role.repository.RolePermissionRepository;
import org.eclipse.tractusx.sde.core.role.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Service
@AllArgsConstructor
public class RoleManagementService {

	private final RoleRepository roleRepository;
	private final RolePermissionRepository rolePermissionRepository;
	private final RolePermissionCustomRepository rolePermissionCustomRepository;

	@SneakyThrows
	@Transactional
	public Map<String,String> saveRoleWithPermission(String role, List<String> rolemappping) {

		checkRoleExistOrNot(List.of(role));

		List<RolePermissionEntity> allentity = rolemappping.stream()
				.map(e -> RolePermissionEntity.builder().sdePermission(e).sdeRole(role).build()).toList();
		rolePermissionRepository.deleteRolePermissionMappingBySdeRole(role);
		rolePermissionRepository.saveAll(allentity);
		return Map.of("msg","Role Permission saved successfully");
	}

	@SneakyThrows
	public List<RolePermissionEntity> findAll(List<String> role, List<String> permission) {
		return rolePermissionCustomRepository.findAll(role, permission);
	}

	@SneakyThrows
	public List<String> getRolePermission(List<String> role) {
		checkRoleExistOrNot(role);
		List<RolePermissionEntity> allPermission = rolePermissionRepository.findAll(role);
		return allPermission.stream().map(RolePermissionEntity::getSdePermission).toList();
	}

	@SneakyThrows
	public RolePojo saveRole(RolePojo role) {
		roleRepository.save(RoleEntity.builder().sdeRole(role.getRole()).description(role.getDescription()).build());
		return role;
	}

	@SneakyThrows
	private void checkRoleExistOrNot(List<String> role) {
		if (roleRepository.findAllById(role).isEmpty())
			throw new NoDataFoundException(role + " role does not exist in SDE");
	}

	public void deleteRole(String role) {
		checkRoleExistOrNot(List.of(role));
		rolePermissionRepository.deleteRolePermissionMappingBySdeRole(role);
		roleRepository.deleteById(role);
	}
}
