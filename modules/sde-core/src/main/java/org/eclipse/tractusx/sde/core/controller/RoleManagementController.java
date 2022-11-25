package org.eclipse.tractusx.sde.core.controller;

import java.util.List;

import org.eclipse.tractusx.sde.core.service.RoleManagementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
	public String fileUpload(@PathVariable("role") String role, @RequestBody List<String> rolePermission) {
		return roleManagementService.saveRoleWithPermission(role, rolePermission);
	}

	@GetMapping(value = "/role/{role}/permissions")
	@PreAuthorize("hasPermission(#role,'read_role_permission')")
	public List<String> getRolePermission(@PathVariable("role") String role) {
		return roleManagementService.getRolePermission(List.of(role));

	}

	@GetMapping(value = "/user/role/permissions")
	@PreAuthorize("hasPermission('','read_role_permission')")
	public List<String> getAllRolePermissions() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		List<String> list = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
		return roleManagementService.getRolePermission(list);

	}
}
