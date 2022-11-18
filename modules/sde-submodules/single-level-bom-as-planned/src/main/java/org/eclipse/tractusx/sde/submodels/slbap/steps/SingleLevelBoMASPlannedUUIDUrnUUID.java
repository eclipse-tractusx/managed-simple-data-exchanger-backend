package org.eclipse.tractusx.sde.submodels.slbap.steps;

import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.submodels.pap.entity.PartAsPlannedEntity;
import org.eclipse.tractusx.sde.submodels.pap.repository.PartAsPlannedRepository;
import org.eclipse.tractusx.sde.submodels.slbap.model.SingleLevelBoMAsPlanned;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;

@Component
public class SingleLevelBoMASPlannedUUIDUrnUUID extends Step {

	private final PartAsPlannedRepository repository;


	public SingleLevelBoMASPlannedUUIDUrnUUID(PartAsPlannedRepository repository) {
		this.repository = repository;
	}
	
	@SneakyThrows
	public SingleLevelBoMAsPlanned run(SingleLevelBoMAsPlanned input, String processId) {
		if (input.getParentUuid() == null || input.getParentUuid().isBlank()) {
			String parentUuid = getUuidIfPartAsPlannedAspectExists(input.getRowNumber(), input.getManufacturerPartId());
			input.setParentUuid(parentUuid);
		}

		if (input.getChildUuid() == null || input.getChildUuid().isBlank()) {
			String childUuid = getUuidIfPartAsPlannedAspectExists(input.getRowNumber(), input.getManufacturerPartId());
			input.setChildUuid(childUuid);
		}

		return input;
	}

	@SneakyThrows
	private String getUuidIfPartAsPlannedAspectExists(int rowNumber, String manufactorerPartId) {
		PartAsPlannedEntity entity = repository.findByManufacturerPartId(manufactorerPartId);

		if (entity == null) {
			throw new CsvHandlerUseCaseException(rowNumber, String.format(
					"Missing parent PartAsPlanned aspect for the given Identifier: ManufactorerPartId: %s ",
					 manufactorerPartId));
		}
		return entity.getUuid();
	}

}
