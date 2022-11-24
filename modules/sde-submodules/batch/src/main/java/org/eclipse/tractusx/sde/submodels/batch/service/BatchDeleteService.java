package org.eclipse.tractusx.sde.submodels.batch.service;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DeleteDigitalTwinsFacilitator;
import org.eclipse.tractusx.sde.edc.facilitator.DeleteEDCFacilitator;
import org.eclipse.tractusx.sde.submodels.batch.entity.BatchEntity;
import org.eclipse.tractusx.sde.submodels.batch.mapper.BatchMapper;
import org.eclipse.tractusx.sde.submodels.batch.repository.BatchRepository;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Service
@AllArgsConstructor
public class BatchDeleteService {

	private final BatchRepository batchRepository;

	private final BatchMapper batchMapper;

	public static final String DELETED_Y = "Y";

	private final DeleteEDCFacilitator deleteEDCFacilitator;

	private final DeleteDigitalTwinsFacilitator deleteDigitalTwinsFacilitator;

	public List<JsonObject> readCreatedTwinsforDelete(String refProcessId) {

		return Optional
				.ofNullable(Optional.ofNullable(batchRepository.findByProcessId(refProcessId)).filter(a -> !a.isEmpty())
						.orElseThrow(() -> new NoDataFoundException(
								String.format("No data found for processid %s ", refProcessId)))
						.stream().filter(e -> !DELETED_Y.equals(e.getDeleted())).map(batchMapper::mapFromEntity)
						.toList())
				.filter(a -> !a.isEmpty()).orElseThrow(
						() -> new NoDataFoundException("No data founds for deletion, All records are already deleted"));
	}

	@SneakyThrows
	public void deleteAllDataBySequence(JsonObject jsonObject) {

		BatchEntity batchEntity = batchMapper.mapforEntity(jsonObject);

		deleteDigitalTwinsFacilitator.deleteDigitalTwinsById(batchEntity.getShellId());

		deleteEDCAsset(batchEntity);

		saveBatchWithDeleted(batchEntity);
	}

	@SneakyThrows
	public void deleteEDCAsset(BatchEntity batchEntity) {

		deleteEDCFacilitator.deleteContractDefination(batchEntity.getContractDefinationId());

		deleteEDCFacilitator.deleteAccessPolicy(batchEntity.getAccessPolicyId());

		deleteEDCFacilitator.deleteUsagePolicy(batchEntity.getUsagePolicyId());

		deleteEDCFacilitator.deleteAssets(batchEntity.getAssetId());
	}

	private void saveBatchWithDeleted(BatchEntity batchEntity) {
		batchEntity.setDeleted(DELETED_Y);
		batchRepository.save(batchEntity);
	}

	public JsonObject readCreatedTwinsDetails(String uuid) {
		return batchMapper.mapToResponse(readEntity(uuid));
	}

	public BatchEntity readEntity(String uuid) {
		return Optional.ofNullable(batchRepository.findByUuid(uuid))
				.orElseThrow(() -> new NoDataFoundException("No data found uuid " + uuid));
	}

	public int getUpdatedData(String refProcessId) {

		return (int) batchRepository.countByUpdatedAndProcessId(CommonConstants.UPDATED_Y, refProcessId);
	}

}
