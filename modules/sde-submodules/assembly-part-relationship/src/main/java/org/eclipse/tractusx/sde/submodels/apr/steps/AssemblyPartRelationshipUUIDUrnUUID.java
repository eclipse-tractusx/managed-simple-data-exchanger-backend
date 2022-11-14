package org.eclipse.tractusx.sde.submodels.apr.steps;

import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.submodels.apr.model.AspectRelationship;
import org.eclipse.tractusx.sde.submodels.spt.entity.AspectEntity;
import org.eclipse.tractusx.sde.submodels.spt.repository.AspectRepository;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;

@Component
public class AssemblyPartRelationshipUUIDUrnUUID extends Step {

	private final AspectRepository repository;


	public AssemblyPartRelationshipUUIDUrnUUID(AspectRepository repository) {
		this.repository = repository;
	}
	
	@SneakyThrows
	public AspectRelationship run(AspectRelationship input, String processId) {
		if (input.getParentUuid() == null || input.getParentUuid().isBlank()) {
			String parentUuid = getUuidIfAspectExists(input.getRowNumber(), input.getParentPartInstanceId(),
					input.getParentManufacturerPartId(), input.getParentOptionalIdentifierKey(),
					input.getParentOptionalIdentifierValue());
			input.setParentUuid(parentUuid);
		}

		if (input.getChildUuid() == null || input.getChildUuid().isBlank()) {
			String childUuid = getUuidIfAspectExists(input.getRowNumber(), input.getChildPartInstanceId(),
					input.getChildManufacturerPartId(), input.getChildOptionalIdentifierKey(),
					input.getChildOptionalIdentifierValue());
			input.setChildUuid(childUuid);
		}

		return input;
	}

	@SneakyThrows
	private String getUuidIfAspectExists(int rowNumber, String partInstanceId, String manufactorerPartId,
			String optionalIdentifierKey, String optionalIdentifierValue) {
		AspectEntity aspect = repository.findByIdentifiers(partInstanceId, manufactorerPartId, optionalIdentifierKey,
				optionalIdentifierValue);

		if (aspect == null) {
			throw new CsvHandlerUseCaseException(rowNumber, String.format(
					"Missing parent aspect for the given Identifier: PartInstanceID: %s | ManufactorerId: %s | %s: %s",
					partInstanceId, manufactorerPartId, optionalIdentifierKey, optionalIdentifierValue));
		}
		return aspect.getUuid();
	}

}
