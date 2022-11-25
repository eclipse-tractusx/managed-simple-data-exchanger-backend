package org.eclipse.tractusx.sde.core.role.entity;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "sde_role_permission_mapping")
@Entity
@Data
@Cacheable(value = false)
@IdClass(RolePermissionMappingId.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionEntity {

	@Id
	@Column(name = "sde_permission")
	private String sdePermission;

	@Id
	@Column(name = "sde_role")
	private String sdeRole;

}
