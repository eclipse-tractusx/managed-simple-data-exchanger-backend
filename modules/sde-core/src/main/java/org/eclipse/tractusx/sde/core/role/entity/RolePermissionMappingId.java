package org.eclipse.tractusx.sde.core.role.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionMappingId {
	
	private String sdePermission;

	private String sdeRole;

}
