package org.eclipse.tractusx.sde.pcfexchange.repository;

import java.util.Optional;

import org.eclipse.tractusx.sde.pcfexchange.entity.PcfResponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PcfReqsponseRepository extends JpaRepository<PcfResponseEntity, String> {

	public Optional<PcfResponseEntity> findFirstByRequestIdOrderByLastUpdatedTimeDesc(String requestId);
}
