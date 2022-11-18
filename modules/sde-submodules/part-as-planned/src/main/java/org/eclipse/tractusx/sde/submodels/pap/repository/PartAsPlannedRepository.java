package org.eclipse.tractusx.sde.submodels.pap.repository;

import java.util.List;

import org.eclipse.tractusx.sde.submodels.pap.entity.PartAsPlannedEntity;
import org.springframework.data.repository.CrudRepository;

public interface PartAsPlannedRepository extends CrudRepository<PartAsPlannedEntity, String> {
	
	PartAsPlannedEntity findByManufacturerPartIdAndNameAtManufacturer(String manufacturerPartId, String nameAtManufacturer);

	PartAsPlannedEntity findByUuid(String uuid);

	List<PartAsPlannedEntity> findByProcessId(String processId);
	
	PartAsPlannedEntity findByManufacturerPartId(String manufacturerPartId);

}
