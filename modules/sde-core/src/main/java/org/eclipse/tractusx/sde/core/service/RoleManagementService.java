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
