package com.catenax.sde.usecases.csvhandler.delete;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.catenax.sde.digitaltwins.gateways.external.DigitalTwinsFeignClient;
import com.catenax.sde.edc.api.EDCFeignClientApi;
import com.catenax.sde.entities.database.AspectEntity;
import com.catenax.sde.entities.database.FailureLogEntity;
import com.catenax.sde.enums.CsvTypeEnum;
import com.catenax.sde.gateways.database.AspectRepository;
import com.catenax.sde.usecases.aspects.GetAspectsUseCase;
import com.catenax.sde.usecases.csvhandler.delete.common.DeleteCommonHelper;
import com.catenax.sde.usecases.logs.FailureLogsUseCase;
import com.catenax.sde.usecases.processreport.ProcessReportUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DeleteUsecaseHandler {
	
	
	@Autowired
	DigitalTwinsFeignClient digitalTwinsFeignClient;
	@Autowired
	EDCFeignClientApi eDCFeignClientApi;
    @Autowired
    private FailureLogsUseCase failureLogsUseCase;
	
    private final ProcessReportUseCase processReportUseCase;
	
    private final AspectRepository aspectRepository;
    private final GetAspectsUseCase aspectsUseCase;
    private final DeleteCommonHelper deleteCommonHelper;
    int deletedRecordCount=0;

    public DeleteUsecaseHandler(GetAspectsUseCase aspectsUseCase,ProcessReportUseCase processReportUseCase,AspectRepository aspectRepository,DeleteCommonHelper deleteCommonHelper) {
		super();
		this.aspectsUseCase = aspectsUseCase;
		this.processReportUseCase = processReportUseCase;
		this.aspectRepository =aspectRepository;
		this.deleteCommonHelper = deleteCommonHelper;
	}
    
    
	public void deleteAspectDigitalTwinsAndEDC(String refProcessId,String deleteProcessId)
			throws JsonProcessingException {

		/*
		 * IMP NOTE: in delete case existing processid will be stored into
		 * referenceProcessid
		 */
		 try{

		List<AspectEntity> listAspect = aspectsUseCase.getListUuidFromProcessId(refProcessId);
		
		if(listAspect.isEmpty())
		{
			throw new RuntimeException("No Aspect Id Associated with Processid ");
		}
		processReportUseCase.startDeleteProcess(deleteProcessId, CsvTypeEnum.ASPECT, listAspect.size(), refProcessId, 0);
		listAspect.parallelStream().forEach((o) -> {
			o.setProcessId(deleteProcessId);
			deleteAllDataBySequence(o);
		});
		processReportUseCase.finalizeNoOfDeletedInProgressReport(deleteProcessId, deletedRecordCount, refProcessId);
		} catch (Exception e) {
			FailureLogEntity entity = FailureLogEntity.builder().uuid(UUID.randomUUID().toString())
					.processId(deleteProcessId).log(e.getMessage()).dateTime(LocalDateTime.now()).build();
			failureLogsUseCase.saveLog(entity);
			log.error(String.format("[%s] %s", this.getClass().getSimpleName(), String.valueOf(e)));
		}

	}
	
	private void deleteAllDataBySequence(AspectEntity aspectEntity) {
		String processId = aspectEntity.getProcessId();
		String assetId = aspectEntity.getAssetId();
		try {
				log.info("Inside Delete deleteAllDataBySequence");
				deleteDigitalTwins(aspectEntity);

				deleteContractDefination(aspectEntity);

				deleteAccessPolicy(aspectEntity);

				deleteUsagePolicy(aspectEntity);

				deleteAssets(assetId);

				saveAspectWithDeleted(aspectEntity);
				log.info("Succestully Deleted Delete");

		} catch (Exception e) {

			FailureLogEntity entity = FailureLogEntity.builder().uuid(UUID.randomUUID().toString()).processId(processId)
					.log(e.getMessage()).dateTime(LocalDateTime.now()).build();
			failureLogsUseCase.saveLog(entity);
			log.error(String.format("[%s] %s", this.getClass().getSimpleName(), String.valueOf(e)));
		}

	}
	
	public void deleteDigitalTwins(AspectEntity aspectEntity) throws Exception {
		try {
			digitalTwinsFeignClient.deleteDigitalTwinsById(aspectEntity.getShellId(),
					deleteCommonHelper.getHeaders());
		} catch (Exception e) {
			deleteCommonHelper.parseExceptionMessage(e);
		}

	}

	public void deleteContractDefination(AspectEntity aspectEntity) throws Exception {
		try {
			eDCFeignClientApi.deleteContractDefinition(aspectEntity.getContractDefinationId(),
					deleteCommonHelper.getEDCHeaders());
		} catch (Exception e) {
			deleteCommonHelper.parseExceptionMessage(e);
		}

	}

	public void deleteAccessPolicy(AspectEntity aspectEntity) throws Exception {
		try {
			eDCFeignClientApi.deletePolicyDefinitions(aspectEntity.getAccessPolicyId(),
					deleteCommonHelper.getEDCHeaders());
		} catch (Exception e) {
			deleteCommonHelper.parseExceptionMessage(e);
		}

	}

	public void deleteUsagePolicy(AspectEntity aspectEntity) throws Exception {
		try {
			eDCFeignClientApi.deletePolicyDefinitions(aspectEntity.getUsagePolicyId(),
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



	private void saveAspectWithDeleted(AspectEntity aspectEntity) {
		aspectEntity.setDeleted(DeleteCommonHelper.DELETED_Y);
		aspectRepository.save(aspectEntity);
		++deletedRecordCount;
	}



}
