package org.eclipse.tractusx.sde.core.pcf.repository;

import java.util.Optional;

import org.eclipse.tractusx.sde.core.pcf.entity.PcfRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PcfRequestRepository extends JpaRepository<PcfRequestEntity, String> {

	Optional<PcfRequestEntity> findByRequestIdAndProductId(String requestId, String productId);

}
