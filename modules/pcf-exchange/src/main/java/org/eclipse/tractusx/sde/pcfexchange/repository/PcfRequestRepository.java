package org.eclipse.tractusx.sde.pcfexchange.repository;

import java.util.Optional;

import org.eclipse.tractusx.sde.pcfexchange.entity.PcfRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PcfRequestRepository extends JpaRepository<PcfRequestEntity, String> {

	Optional<PcfRequestEntity> findByRequestIdAndProductIdAndBpnNumber(String requestId, String productId,
			String bpnNumber);

	Page<PcfRequestEntity> findByStatusLikeOrderByLastUpdatedTimeDesc(PageRequest of, String status);

}
