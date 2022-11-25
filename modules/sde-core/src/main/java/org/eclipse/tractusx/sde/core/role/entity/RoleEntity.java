package org.eclipse.tractusx.sde.core.role.entity;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Table(name = "sde_role")
@Entity
@Data
@Cacheable(value = false)
public class RoleEntity {

	@Id
	private String sdeRole;
	
	private String description;
}
