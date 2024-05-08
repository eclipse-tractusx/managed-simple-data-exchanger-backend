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

package org.eclipse.tractusx.sde.core.controller;

import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.core.role.entity.RolePojo;
import org.eclipse.tractusx.sde.core.service.RoleManagementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class RoleManagementController {

	private final RoleManagementService roleManagementService;

	@PostMapping(value = "/role/{role}/permissions")
	@PreAuthorize("hasPermission(#role,'create_role')")
	public Map<String,String> saveRolePermission(@PathVariable("role") String role, @RequestBody List<String> rolePermission) {
		return roleManagementService.saveRoleWithPermission(role, rolePermission);
	}
	
	@PostMapping(value = "/role")
	@PreAuthorize("hasPermission(#role,'create_role')")
	public RolePojo saveRole(@RequestBody RolePojo role) {
		return roleManagementService.saveRole(role);
	}
	
	@DeleteMapping(value = "/role/{role}")
	@PreAuthorize("hasPermission(#role,'delete_role')")
	public void deleteRole(@PathVariable("role") String role) {
		roleManagementService.deleteRole(role);
	}

	@GetMapping(value = "/role/{role}/permissions")
	@PreAuthorize("hasPermission(#role,'read_role_permission')")
	public List<String> getRolePermission(@PathVariable("role") String role) {
		return roleManagementService.getRolePermission(List.of(role));

	}

	@GetMapping(value = "/user/role/permissions")
	public List<String> getAllRolePermissions() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		List<String> list = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
		return roleManagementService.getRolePermission(list);

	}
}
