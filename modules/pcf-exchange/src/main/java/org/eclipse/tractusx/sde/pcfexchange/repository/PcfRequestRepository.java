package org.eclipse.tractusx.sde.pcfexchange.repository;

import java.util.Optional;

import org.eclipse.tractusx.sde.pcfexchange.entity.PcfRequestEntity;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFRequestStatusEnum;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PcfRequestRepository extends JpaRepository<PcfRequestEntity, String> {

	Optional<PcfRequestEntity> findByRequestIdAndProductIdAndBpnNumber(String requestId, String productId,
			String bpnNumber);
	
	Page<PcfRequestEntity> findByType(Pageable pageable, PCFTypeEnum type);
	
	Page<PcfRequestEntity> findByTypeAndStatus(Pageable pageable, PCFTypeEnum type, PCFRequestStatusEnum status);

	Page<PcfRequestEntity> findByStatusAndTypeLikeOrderByLastUpdatedTimeDesc(PageRequest of, PCFRequestStatusEnum status, PCFTypeEnum type);

}
