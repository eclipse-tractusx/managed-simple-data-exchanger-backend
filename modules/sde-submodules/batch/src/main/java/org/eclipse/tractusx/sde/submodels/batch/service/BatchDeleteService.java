package org.eclipse.tractusx.sde.submodels.batch.service;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DeleteDigitalTwinsFacilitator;
import org.eclipse.tractusx.sde.edc.facilitator.DeleteEDCFacilitator;
import org.eclipse.tractusx.sde.submodels.batch.entity.BatchEntity;
import org.eclipse.tractusx.sde.submodels.batch.mapper.BatchMapper;
import org.eclipse.tractusx.sde.submodels.batch.repository.BatchRepository;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BatchDeleteService {

	private final BatchRepository batchRepository;

	private final BatchMapper batchMapper;

	public static final String DELETED_Y = "Y";

	private final DeleteEDCFacilitator deleteEDCFacilitator;

	private final DeleteDigitalTwinsFacilitator deleteDigitalTwinsFacilitator;

	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {
		
		return Optional.ofNullable(
				Optional.ofNullable(batchRepository.findByProcessId(refProcessId))
				.filter(a -> !a.isEmpty())
		        .orElseThrow(() -> new NoDataFoundException(String.format("No data found for processid %s ", refProcessId)))
				.stream().filter(e -> !DELETED_Y.equals(e.getDeleted()))
				.map(batchMapper::mapFromEntity)
				.toList())
				.filter(a -> !a.isEmpty())
				.orElseThrow(() -> new NoDataFoundException("No data founds for deletion, All records are already deleted"));
	}

	public void deleteAllDataBySequence(JsonObject jsonObject) {

		BatchEntity batchEntity = batchMapper.mapforEntity(jsonObject);

		deleteDigitalTwinsFacilitator.deleteDigitalTwinsById(batchEntity.getShellId());

		deleteEDCFacilitator.deleteContractDefination(batchEntity.getContractDefinationId());

		deleteEDCFacilitator.deleteAccessPolicy(batchEntity.getAccessPolicyId());

		deleteEDCFacilitator.deleteUsagePolicy(batchEntity.getUsagePolicyId());

		deleteEDCFacilitator.deleteAssets(batchEntity.getAssetId());

		saveBatchWithDeleted(batchEntity);
	}

	private void saveBatchWithDeleted(BatchEntity batchEntity) {
		batchEntity.setDeleted(DELETED_Y);
		batchRepository.save(batchEntity);
	}

	public JsonObject readCreatedTwinsDetails(String uuid) {
		return batchMapper.mapToResponse(batchRepository.findByUuid(uuid));
	}

}
