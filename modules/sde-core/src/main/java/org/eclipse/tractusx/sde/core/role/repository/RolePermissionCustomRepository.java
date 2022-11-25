package org.eclipse.tractusx.sde.core.role.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.eclipse.tractusx.sde.core.role.entity.RolePermissionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RolePermissionCustomRepository {
	
	@Autowired
    private EntityManager entityManager;
	
	@SuppressWarnings("unchecked")
	public List<RolePermissionEntity> findAll(List<String> role, List<String> permission) {
		Query query = entityManager.createQuery("SELECT p FROM RolePermissionEntity p Where p.sdeRole IN :role and p.sdePermission IN :permission ");
		query.setParameter("role", role);
		query.setParameter("permission", permission);
		return query.getResultList();
	} 
	

}
