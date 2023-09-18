package org.eclipse.tractusx.sde.core.policy.repository;

import org.eclipse.tractusx.sde.core.policy.entity.PolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRepository extends JpaRepository<PolicyEntity, String> {
}
