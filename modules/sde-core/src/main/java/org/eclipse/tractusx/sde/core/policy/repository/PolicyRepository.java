package org.eclipse.tractusx.sde.core.policy.repository;

import org.eclipse.tractusx.sde.core.policy.entity.PolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PolicyRepository extends JpaRepository<PolicyEntity, String> {

    Optional<PolicyEntity> findByName(String name);
}
