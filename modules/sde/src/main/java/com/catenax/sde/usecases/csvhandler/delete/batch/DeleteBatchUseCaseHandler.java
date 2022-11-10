package com.catenax.sde.usecases.csvhandler.delete.batch;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.catenax.sde.digitaltwins.gateways.external.DigitalTwinsFeignClient;
import com.catenax.sde.edc.api.EDCFeignClientApi;
import com.catenax.sde.entities.database.BatchEntity;
import com.catenax.sde.entities.database.FailureLogEntity;
import com.catenax.sde.enums.CsvTypeEnum;
import com.catenax.sde.gateways.database.BatchRepository;
import com.catenax.sde.usecases.batchs.GetBatchsUseCase;
import com.catenax.sde.usecases.csvhandler.delete.common.DeleteCommonHelper;
import com.catenax.sde.usecases.logs.FailureLogsUseCase;
import com.catenax.sde.usecases.processreport.ProcessReportUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DeleteBatchUseCaseHandler {

	@Autowired
	DigitalTwinsFeignClient digitalTwinsFeignClient;
	@Autowired
	EDCFeignClientApi eDCFeignClientApi;
	@Autowired
	private FailureLogsUseCase failureLogsUseCase;

	private final ProcessReportUseCase processReportUseCase;
	private final GetBatchsUseCase batchUseCase;

	private final DeleteCommonHelper deleteCommonHelper;
	private final BatchRepository batchRepository;

	int deletedRecordCount=0;
	public DeleteBatchUseCaseHandler(FailureLogsUseCase failureLogsUseCase, ProcessReportUseCase processReportUseCase,
			GetBatchsUseCase batchUseCase, DeleteCommonHelper deleteCommonHelper, BatchRepository batchRepository) {
		super();
		this.failureLogsUseCase = failureLogsUseCase;
		this.processReportUseCase = processReportUseCase;
		this.batchUseCase = batchUseCase;
		this.deleteCommonHelper = deleteCommonHelper;
		this.batchRepository = batchRepository;
	}

	public void deleteBatchDigitalTwinsAndEDC(String refProcessId, String deleteProcessId)
			throws JsonProcessingException {

		/*
		 * IMP NOTE: in delete case existing processid will be stored into
		 * referenceProcessid
		 */

		try {
			List<BatchEntity> listBatch = batchUseCase.getListUuidFromProcessId(refProcessId);
			if(listBatch.isEmpty())
			{
				throw new RuntimeException("No Aspect Id Associated with Processid ");
			}
			processReportUseCase.startDeleteProcess(deleteProcessId, CsvTypeEnum.BATCH, listBatch.size(), refProcessId,
					0);
			
			listBatch.parallelStream().forEach((o) -> {
				o.setProcessId(deleteProcessId);
				deleteAllDataBySequence(o);

			});
			processReportUseCase.finalizeNoOfDeletedInProgressReport(deleteProcessId, deletedRecordCount,
					refProcessId);
		} catch (Exception e) {
			FailureLogEntity entity = FailureLogEntity.builder().uuid(UUID.randomUUID().toString())
					.processId(deleteProcessId).log(e.getMessage()).dateTime(LocalDateTime.now()).build();
			failureLogsUseCase.saveLog(entity);
			log.error(String.format("[%s] %s", this.getClass().getSimpleName(), String.valueOf(e)));
		}
	}

	private void deleteAllDataBySequence(BatchEntity batchEntity) {
		String processId = batchEntity.getProcessId();
		String assetId = batchEntity.getAssetId();
		try {
			
			log.info("Inside Delete deleteAllDataBySequence");
		
				deleteDigitalTwins(batchEntity);

				deleteContractDefination(batchEntity);
				
				deleteAccessPolicy(batchEntity);
				
				deleteUsagePolicy(batchEntity);
				
				deleteAssets(assetId);	
						
				saveBatchWithDeleted(batchEntity);
				log.info("End Delete deleteAllDataBySequence");
			}
		 catch (Exception e) {

			FailureLogEntity entity = FailureLogEntity.builder().uuid(UUID.randomUUID().toString()).processId(processId)
					.log(e.getMessage()).dateTime(LocalDateTime.now()).build();
			failureLogsUseCase.saveLog(entity);
			log.error(String.format("[%s] %s", this.getClass().getSimpleName(), String.valueOf(e)));
		}

	}
	
	public void deleteDigitalTwins(BatchEntity batchEntity) throws Exception
	{
		try {
			digitalTwinsFeignClient.deleteDigitalTwinsById(batchEntity.getShellId(),
					deleteCommonHelper.getHeaders());
		}catch(Exception e)
		{
			deleteCommonHelper.parseExceptionMessage(e);
		}
		
	}
	
	public void deleteContractDefination(BatchEntity batchEntity) throws Exception
	{
		try {
			eDCFeignClientApi.deleteContractDefinition(
					batchEntity.getContractDefinationId(), deleteCommonHelper.getEDCHeaders());
		}catch(Exception e)
		{
			deleteCommonHelper.parseExceptionMessage(e);
		}
		
	}
	
	public void deleteAccessPolicy(BatchEntity batchEntity) throws Exception
	{
		try {
			eDCFeignClientApi.deletePolicyDefinitions(
					batchEntity.getAccessPolicyId(), deleteCommonHelper.getEDCHeaders());
		}catch(Exception e)
		{
			deleteCommonHelper.parseExceptionMessage(e);
		}
		
	}
	
	public void deleteUsagePolicy(BatchEntity batchEntity) throws Exception
	{
		try {
			eDCFeignClientApi.deletePolicyDefinitions(
					batchEntity.getUsagePolicyId(), deleteCommonHelper.getEDCHeaders());
		}catch(Exception e)
		{
			deleteCommonHelper.parseExceptionMessage(e);
		}
		
	}
	
	public void deleteAssets(String assetId) throws Exception
	{
		try {
			eDCFeignClientApi.deleteAssets(assetId,
					deleteCommonHelper.getEDCHeaders());
		}catch(Exception e)
		{
			deleteCommonHelper.parseExceptionMessage(e);
		}
		
	}
	
	private void saveBatchWithDeleted(BatchEntity batchEntity) {
		batchEntity.setDeleted(DeleteCommonHelper.DELETED_Y);
		 batchRepository.save(batchEntity);
		 ++deletedRecordCount;
	}

}
