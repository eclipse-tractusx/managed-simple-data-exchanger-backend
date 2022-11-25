package org.eclipse.tractusx.sde.configuration.role;

import java.io.Serializable;
import java.util.List;

import org.eclipse.tractusx.sde.core.service.RoleManagementService;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

	private final RoleManagementService roleManagementService;

	@Override
	public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
		if ((auth == null) || (targetDomainObject == null) || !(permission instanceof String)) {
			return false;
		}
		return hasPrivilege(auth, permission.toString().toUpperCase());
	}

	@Override
	public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
		if ((auth == null) || (targetType == null) || !(permission instanceof String)) {
			return false;
		}
		return hasPrivilege(auth, permission.toString().toUpperCase());
	}

	private boolean hasPrivilege(Authentication auth, String permission) {

		String[] permissionLs = permission.toLowerCase().split("@");

		List<String> list = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

		return !roleManagementService.findAll(list, List.of(permissionLs)).isEmpty();
	}
}