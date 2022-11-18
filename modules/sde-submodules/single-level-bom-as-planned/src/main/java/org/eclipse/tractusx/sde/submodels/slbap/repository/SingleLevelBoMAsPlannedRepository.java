package org.eclipse.tractusx.sde.submodels.slbap.repository;

import java.util.List;

import org.eclipse.tractusx.sde.submodels.slbap.entity.SingleLevelBoMAsPlannedEntity;
import org.springframework.data.repository.CrudRepository;

public interface SingleLevelBoMAsPlannedRepository extends CrudRepository<SingleLevelBoMAsPlannedEntity, String> {

	List<SingleLevelBoMAsPlannedEntity> findByProcessId(String processId);
	
    List<SingleLevelBoMAsPlannedEntity> findByParentCatenaXId(String parentCatenaXId);
    

}
