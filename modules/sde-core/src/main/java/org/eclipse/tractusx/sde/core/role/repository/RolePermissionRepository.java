package org.eclipse.tractusx.sde.core.role.repository;

import java.util.List;

import org.eclipse.tractusx.sde.core.role.entity.RolePermissionEntity;
import org.eclipse.tractusx.sde.core.role.entity.RolePermissionMappingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, RolePermissionMappingId> {

	@Query("SELECT p FROM RolePermissionEntity p Where p.sdeRole IN ?1")
	List<RolePermissionEntity> findAll(List<String> sdeRole);

}
