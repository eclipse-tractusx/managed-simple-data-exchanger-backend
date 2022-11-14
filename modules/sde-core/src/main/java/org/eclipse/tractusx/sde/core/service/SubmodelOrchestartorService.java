package org.eclipse.tractusx.sde.core.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.tractusx.sde.common.entities.SubmodelFileRequest;
import org.eclipse.tractusx.sde.common.entities.SubmodelJsonRequest;
import org.eclipse.tractusx.sde.common.entities.csv.CsvContent;
import org.eclipse.tractusx.sde.common.enums.ProgressStatusEnum;
import org.eclipse.tractusx.sde.common.mapper.JsonObjectMapper;
import org.eclipse.tractusx.sde.common.mapper.SubmodelMapper;
import org.eclipse.tractusx.sde.common.model.Submodel;
import org.eclipse.tractusx.sde.common.submodel.executor.SubmodelExecutor;
import org.eclipse.tractusx.sde.common.validators.SubmodelCSVValidator;
import org.eclipse.tractusx.sde.core.controller.failurelog.FailureLogs;
import org.eclipse.tractusx.sde.core.processreport.ProcessReportUseCase;
import org.eclipse.tractusx.sde.core.processreport.model.ProcessReport;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubmodelOrchestartorService {

	private final SubmodelCSVValidator sumodelcsvValidator;

	private final SubmodelService submodelService;
	
	private final SubmodelMapper submodelMapper;

	private final JsonObjectMapper jsonObjectMapper;

	private final ProcessReportUseCase processReportUseCase;

	private final FailureLogs failureLogs;

	public void processSubmodelCsv(CsvContent csvContent, SubmodelFileRequest submodelFileRequest, String processId,
			String submodel) {

		Submodel submodelSchemaObject = submodelService.findSubmodelByNameAsSubmdelObject(submodel);
		JsonObject submodelSchema= submodelSchemaObject.getSchema();
		JsonObject items = submodelSchema.get("items").getAsJsonObject();
		JsonObject asJsonObject = items.get("properties").getAsJsonObject();

		List<String> columns = csvContent.getColumns();

		sumodelcsvValidator.validate(asJsonObject, columns, submodel);

		Runnable runnable = () -> {
			processReportUseCase.startBuildProcessReport(processId, submodelSchemaObject.getId(), csvContent.getRows().size(),
					submodelFileRequest.getBpnNumbers(), submodelFileRequest.getTypeOfAccess(),
					submodelFileRequest.getUsagePolicies());

			AtomicInteger successCount = new AtomicInteger();
			AtomicInteger failureCount = new AtomicInteger();

			csvContent.getRows().parallelStream().forEach(rowjObj -> {
				try {
					JsonObject newjObject = jsonObjectMapper.submodelFileRequestToJsonPojo(submodelFileRequest);
					newjObject.addProperty("row_number", rowjObj.position());
					newjObject.addProperty("process_id", processId);
					SubmodelExecutor executor = submodelSchemaObject.getExecutor();
					executor.init(submodelSchema);
					executor.executeCsvRecord(rowjObj, newjObject, processId);
					successCount.incrementAndGet();
				} catch (Exception e) {
					failureLogs.saveLog(processId, e.getMessage());
					failureCount.incrementAndGet();
				}
			});
			processReportUseCase.finishBuildProgressReport(processId, successCount.get(), failureCount.get());
		};

		new Thread(runnable).start();

	}

	public void processSubmodel(SubmodelJsonRequest<ObjectNode> submodelJsonRequest, String processId,
			String submodel) {

		Submodel submodelSchemaObject = submodelService.findSubmodelByNameAsSubmdelObject(submodel);
		JsonObject submodelSchema= submodelSchemaObject.getSchema();

		List<ObjectNode> rowData = submodelJsonRequest.getRowData();

		Runnable runnable = () -> {

			AtomicInteger atInt = new AtomicInteger();
			AtomicInteger successCount = new AtomicInteger();
			AtomicInteger failureCount = new AtomicInteger();
			
			processReportUseCase.startBuildProcessReport(processId, submodelSchemaObject.getId(), rowData.size(),
					submodelJsonRequest.getBpnNumbers(), submodelJsonRequest.getTypeOfAccess(),
					submodelJsonRequest.getUsagePolicies());
			rowData.parallelStream().forEachOrdered(rowjObj -> {
				try {
					JsonObject submodelJsonPojo = jsonObjectMapper.submodelJsonRequestToJsonPojo(rowjObj,
							submodelJsonRequest);
					int andIncrement = atInt.incrementAndGet();
					submodelJsonPojo.addProperty("row_number", andIncrement);
					submodelJsonPojo.addProperty("process_id", processId);
					SubmodelExecutor executor = submodelSchemaObject.getExecutor();
					executor.init(submodelSchema);
					executor.executeJsonRecord(andIncrement, submodelJsonPojo, processId);
					successCount.incrementAndGet();
				} catch (Exception e) {
					failureLogs.saveLog(processId, e.getMessage());
					failureCount.incrementAndGet();
				}
			});
			processReportUseCase.finishBuildProgressReport(processId, successCount.get(), failureCount.get());
		};
		new Thread(runnable).start();
	}

	public void deleteSubmodelDigitalTwinsAndEDC(String refProcessId, String delProcessId, String submodel) {

		Submodel submodelSchema = submodelService.findSubmodelByNameAsSubmdelObject(submodel);
		AtomicInteger deletedCount = new AtomicInteger();
		AtomicInteger failureCount = new AtomicInteger();
		
		SubmodelExecutor executor = submodelSchema.getExecutor();
		
		ProcessReport processReportById = processReportUseCase.getProcessReportById(refProcessId);
		
		List<JsonObject> readCreatedTwinsforDelete = executor.readCreatedTwinsforDelete(refProcessId);
		
		Runnable runnable = () -> {

			processReportById.setProcessId(delProcessId);
			processReportById.setCsvType(submodel.toUpperCase());
			processReportById.setStatus(ProgressStatusEnum.IN_PROGRESS);
			processReportById.setNumberOfItems(readCreatedTwinsforDelete.size());
			processReportById.setStartDate(LocalDateTime.now());
			processReportById.setReferenceProcessId(refProcessId);
			processReportById.setNumberOfDeletedItems(0);
			
			processReportUseCase.saveProcessReport(processReportById);
			
			readCreatedTwinsforDelete.parallelStream().forEach(rowjObj -> {
				try {
					executor.executeDeleteRecord(rowjObj, delProcessId, refProcessId);
					deletedCount.incrementAndGet();
				} catch (Exception e) {
					failureLogs.saveLog(delProcessId, e.getMessage());
					failureCount.incrementAndGet();
				}
			});
			processReportUseCase.finishBuildDeleteProgressReport(delProcessId, deletedCount.get(), failureCount.get());
		};
		new Thread(runnable).start();

	}

	public Map<Object,Object> readCreatedTwinsDetails(String submodel, String uuid) {
		Submodel submodelSchema = submodelService.findSubmodelByNameAsSubmdelObject(submodel);
		SubmodelExecutor executor = submodelSchema.getExecutor();
		return submodelMapper.jsonPojoToMap(executor.readCreatedTwinsDetails(uuid));
	}

}
