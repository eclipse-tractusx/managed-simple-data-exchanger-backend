package org.eclipse.tractusx.sde.submodels.slbap.steps;

import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.submodels.slbap.entity.SingleLevelBoMAsPlannedEntity;
import org.eclipse.tractusx.sde.submodels.slbap.mapper.SingleLevelBoMAsPlannedMapper;
import org.eclipse.tractusx.sde.submodels.slbap.model.SingleLevelBoMAsPlanned;
import org.eclipse.tractusx.sde.submodels.slbap.repository.SingleLevelBoMAsPlannedRepository;
import org.springframework.stereotype.Service;

@Service
public class StoreSingleLevelBoMAsPlannedStep extends Step {

	private final SingleLevelBoMAsPlannedRepository singleLevelBoMAsPlannedRepository;
	private final SingleLevelBoMAsPlannedMapper singleLevelBoMAsPlannedMapper;

	public StoreSingleLevelBoMAsPlannedStep(SingleLevelBoMAsPlannedRepository singleLevelBoMAsPlannedRepository, SingleLevelBoMAsPlannedMapper mapper) {
		this.singleLevelBoMAsPlannedRepository = singleLevelBoMAsPlannedRepository;
		this.singleLevelBoMAsPlannedMapper = mapper;
	}

	public SingleLevelBoMAsPlanned run(SingleLevelBoMAsPlanned input) {
		SingleLevelBoMAsPlannedEntity entity = singleLevelBoMAsPlannedMapper.mapFrom(input);
		singleLevelBoMAsPlannedRepository.save(entity);
		return input;
	}
}
