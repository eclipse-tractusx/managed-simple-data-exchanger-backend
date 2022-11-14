package org.eclipse.tractusx.sde.submodels.apr.service;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DeleteDigitalTwinsFacilitator;
import org.eclipse.tractusx.sde.edc.facilitator.DeleteEDCFacilitator;
import org.eclipse.tractusx.sde.submodels.apr.entity.AspectRelationshipEntity;
import org.eclipse.tractusx.sde.submodels.apr.mapper.AspectRelationshipMapper;
import org.eclipse.tractusx.sde.submodels.apr.repository.AspectRelationshipRepository;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AspectRelationshipService {

	private final AspectRelationshipRepository aspectRelationshipRepository;

	private final AspectRelationshipMapper aspectRelationshipMapper;

	public static final String DELETED_Y = "Y";

	private final DeleteEDCFacilitator deleteEDCFacilitator;

	private final DeleteDigitalTwinsFacilitator deleteDigitalTwinsFacilitator;

	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {

		return Optional
				.ofNullable(Optional.ofNullable(aspectRelationshipRepository.findByProcessId(refProcessId))
						.filter(a -> !a.isEmpty())
						.orElseThrow(() -> new NoDataFoundException(
								String.format("No data found for processid %s ", refProcessId)))
						.stream().filter(e -> !DELETED_Y.equals(e.getDeleted()))
						.map(aspectRelationshipMapper::mapFromEntity).toList())
				.filter(a -> !a.isEmpty()).orElseThrow(
						() -> new NoDataFoundException("No data founds for deletion, All records are already deleted"));
	}

	public void deleteAllDataBySequence(JsonObject jsonObject) {

		AspectRelationshipEntity aspectRelationshipEntity = aspectRelationshipMapper.mapforEntity(jsonObject);

		deleteDigitalTwinsFacilitator.deleteDigitalTwinsById(aspectRelationshipEntity.getShellId());

		deleteEDCFacilitator.deleteContractDefination(aspectRelationshipEntity.getContractDefinationId());

		deleteEDCFacilitator.deleteAccessPolicy(aspectRelationshipEntity.getAccessPolicyId());

		deleteEDCFacilitator.deleteUsagePolicy(aspectRelationshipEntity.getUsagePolicyId());

		deleteEDCFacilitator.deleteAssets(aspectRelationshipEntity.getAssetId());

		saveAspectRelationshipWithDeleted(aspectRelationshipEntity);
	}

	private void saveAspectRelationshipWithDeleted(AspectRelationshipEntity aspectRelationshipEntity) {
		aspectRelationshipEntity.setDeleted(DELETED_Y);
		aspectRelationshipRepository.save(aspectRelationshipEntity);
	}

	public JsonObject readCreatedTwinsDetails(String uuid) {
		List<AspectRelationshipEntity> entities = aspectRelationshipRepository.findByParentCatenaXId(uuid);
		return aspectRelationshipMapper.mapToResponse(uuid, entities);
	}

}
