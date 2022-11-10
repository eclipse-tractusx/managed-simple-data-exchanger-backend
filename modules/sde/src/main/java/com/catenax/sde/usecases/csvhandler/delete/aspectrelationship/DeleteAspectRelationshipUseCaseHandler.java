package com.catenax.sde.usecases.csvhandler.delete.aspectrelationship;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.catenax.sde.digitaltwins.gateways.external.DigitalTwinsFeignClient;
import com.catenax.sde.edc.api.EDCFeignClientApi;
import com.catenax.sde.entities.database.AspectRelationshipEntity;
import com.catenax.sde.entities.database.FailureLogEntity;
import com.catenax.sde.enums.CsvTypeEnum;
import com.catenax.sde.gateways.database.AspectRelationshipRepository;
import com.catenax.sde.usecases.aspectrelationship.GetAspectsRelationshipUseCase;
import com.catenax.sde.usecases.csvhandler.delete.common.DeleteCommonHelper;
import com.catenax.sde.usecases.logs.FailureLogsUseCase;
import com.catenax.sde.usecases.processreport.ProcessReportUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DeleteAspectRelationshipUseCaseHandler {



	@Autowired
	DigitalTwinsFeignClient digitalTwinsFeignClient;
	@Autowired
	EDCFeignClientApi eDCFeignClientApi;
    @Autowired
    private FailureLogsUseCase failureLogsUseCase;
	
    private final ProcessReportUseCase processReportUseCase;
    private final GetAspectsRelationshipUseCase aspectsRelationshipUseCase;
    
    private final DeleteCommonHelper deleteCommonHelper;
    private final AspectRelationshipRepository aspectRelationshipRepository;
    int deletedRecordCount=0;

	public DeleteAspectRelationshipUseCaseHandler(FailureLogsUseCase failureLogsUseCase,
			ProcessReportUseCase processReportUseCase, GetAspectsRelationshipUseCase aspectsRelationshipUseCase,
			DeleteCommonHelper deleteCommonHelper, AspectRelationshipRepository aspectRelationshipRepository) {
		super();
		this.failureLogsUseCase = failureLogsUseCase;
		this.processReportUseCase = processReportUseCase;
		this.aspectsRelationshipUseCase = aspectsRelationshipUseCase;
		this.deleteCommonHelper = deleteCommonHelper;
		this.aspectRelationshipRepository = aspectRelationshipRepository;
	}

	public void deleteAspectRelationshipDigitalTwinsAndEDC(String refProcessId, String deleteProcessId)
			throws JsonProcessingException {

		/*
		 * IMP NOTE: in delete case existing processid will be stored into
		 * referenceProcessid
		 */
		
		try {
			log.info("Inside Delete deleteAspectRelationshipDigitalTwinsAndEDC");
			List<AspectRelationshipEntity> listAspectRelationship = aspectsRelationshipUseCase
					.getListUuidFromProcessId(refProcessId);
			if(listAspectRelationship.isEmpty())
			{
				throw new RuntimeException("No Aspect Id Associated with Processid ");
			}
			processReportUseCase.startDeleteProcess(deleteProcessId, CsvTypeEnum.ASPECT_RELATIONSHIP,
					listAspectRelationship.size(), refProcessId, 0);
			listAspectRelationship.parallelStream().forEach((o) -> {
				o.setProcessId(deleteProcessId);
				deleteAllDataBySequence(o);

			});
			log.info("Deleted all Data with processID reference.");
			processReportUseCase.finalizeNoOfDeletedInProgressReport(deleteProcessId, deletedRecordCount, refProcessId);
		}catch(Exception e) {
			FailureLogEntity entity = FailureLogEntity.builder().uuid(UUID.randomUUID().toString()).processId(deleteProcessId)
					.log(e.getMessage()).dateTime(LocalDateTime.now()).build();
			failureLogsUseCase.saveLog(entity);
			log.error(String.format("[%s] %s", this.getClass().getSimpleName(), String.valueOf(e)));
		}
		

	}
	
	private void deleteAllDataBySequence(AspectRelationshipEntity aspectRelationshipEntity) {
		String processId = aspectRelationshipEntity.getProcessId();
		String assetId = aspectRelationshipEntity.getAssetId();
		try {
			
				log.info("Inside Delete deleteAllDataBySequence");
				deleteDigitalTwins(aspectRelationshipEntity);
				deleteContractDefination(aspectRelationshipEntity);

				deleteAccessPolicy(aspectRelationshipEntity);

				deleteUsagePolicy(aspectRelationshipEntity);

				deleteAssets(assetId);

				saveAspectRelationshipWithDeleted(aspectRelationshipEntity);
				
				log.info("End Delete deleteAllDataBySequence");

		
		} catch (Exception e) {

			FailureLogEntity entity = FailureLogEntity.builder().uuid(UUID.randomUUID().toString()).processId(processId)
					.log(e.getMessage()).dateTime(LocalDateTime.now()).build();
			failureLogsUseCase.saveLog(entity);
			log.error(String.format("[%s] %s", this.getClass().getSimpleName(), String.valueOf(e)));
		}

	}
	
	public void deleteDigitalTwins(AspectRelationshipEntity aspectRelationshipEntity) throws Exception {
		try {
			digitalTwinsFeignClient
					.deleteDigitalTwinsById(aspectRelationshipEntity.getShellId(), deleteCommonHelper.getHeaders());
		} catch (Exception e) {
			deleteCommonHelper.parseExceptionMessage(e);
		}

	}
	
	public void deleteContractDefination(AspectRelationshipEntity aspectRelationshipEntity) throws Exception {
		try {
			eDCFeignClientApi.deleteContractDefinition(aspectRelationshipEntity.getContractDefinationId(),
					deleteCommonHelper.getEDCHeaders());
		} catch (Exception e) {
			deleteCommonHelper.parseExceptionMessage(e);
		}

	}

	public void deleteAccessPolicy(AspectRelationshipEntity aspectRelationshipEntity) throws Exception {
		try {
			eDCFeignClientApi.deletePolicyDefinitions(aspectRelationshipEntity.getAccessPolicyId(),
					deleteCommonHelper.getEDCHeaders());
		} catch (Exception e) {
			deleteCommonHelper.parseExceptionMessage(e);
		}

	}

	public void deleteUsagePolicy(AspectRelationshipEntity aspectRelationshipEntity) throws Exception {
		try {
			eDCFeignClientApi.deletePolicyDefinitions(aspectRelationshipEntity.getUsagePolicyId(),
					deleteCommonHelper.getEDCHeaders());
		} catch (Exception e) {
			deleteCommonHelper.parseExceptionMessage(e);
		}

	}

	public void deleteAssets(String assetId) throws Exception {
		try {
			eDCFeignClientApi.deleteAssets(assetId, deleteCommonHelper.getEDCHeaders());
		} catch (Exception e) {
			deleteCommonHelper.parseExceptionMessage(e);
		}

	}

	private void saveAspectRelationshipWithDeleted(AspectRelationshipEntity aspectRelationshipEntity) {
		aspectRelationshipEntity.setDeleted(DeleteCommonHelper.DELETED_Y);
		aspectRelationshipRepository.save(aspectRelationshipEntity);
		++deletedRecordCount;
	}

}
