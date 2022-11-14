package org.eclipse.tractusx.sde.submodels.spt.service;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DeleteDigitalTwinsFacilitator;
import org.eclipse.tractusx.sde.edc.facilitator.DeleteEDCFacilitator;
import org.eclipse.tractusx.sde.submodels.spt.entity.AspectEntity;
import org.eclipse.tractusx.sde.submodels.spt.mapper.AspectMapper;
import org.eclipse.tractusx.sde.submodels.spt.repository.AspectRepository;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AspectService {

	private final AspectRepository aspectRepository;

	private final AspectMapper aspectMapper;

	public static final String DELETED_Y = "Y";

	private final DeleteEDCFacilitator deleteEDCFacilitator;

	private final DeleteDigitalTwinsFacilitator deleteDigitalTwinsFacilitator;

	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {
		
		return Optional.ofNullable(
				Optional.ofNullable(aspectRepository.findByProcessId(refProcessId))
				.filter(a -> !a.isEmpty())
		        .orElseThrow(() -> new NoDataFoundException(String.format("No data found for processid %s ", refProcessId)))
				.stream().filter(e -> !DELETED_Y.equals(e.getDeleted()))
				.map(aspectMapper::mapFromEntity)
				.toList())
				.filter(a -> !a.isEmpty())
				.orElseThrow(() -> new NoDataFoundException("No data founds for deletion, All records are already deleted"));
		
	}

	public void deleteAllDataBySequence(JsonObject jsonObject) {

		AspectEntity aspectEntity = aspectMapper.mapforEntity(jsonObject);

		deleteDigitalTwinsFacilitator.deleteDigitalTwinsById(aspectEntity.getShellId());

		deleteEDCFacilitator.deleteContractDefination(aspectEntity.getContractDefinationId());

		deleteEDCFacilitator.deleteAccessPolicy(aspectEntity.getAccessPolicyId());

		deleteEDCFacilitator.deleteUsagePolicy(aspectEntity.getUsagePolicyId());

		deleteEDCFacilitator.deleteAssets(aspectEntity.getAssetId());

		saveAspectWithDeleted(aspectEntity);
	}

	private void saveAspectWithDeleted(AspectEntity aspectEntity) {
		aspectEntity.setDeleted(DELETED_Y);
		aspectRepository.save(aspectEntity);
	}

	public JsonObject readCreatedTwinsDetails(String uuid) {
		return aspectMapper.mapToResponse(aspectRepository.findByUuid(uuid));
	}
	
	

}
