/********************************************************************************
 * Copyright (c) 2022, 2023 T-Systems International GmbH
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.tractusx.sde.core.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.entities.PolicyTemplateRequest;
import org.eclipse.tractusx.sde.common.entities.PolicyTemplateType;
import org.eclipse.tractusx.sde.common.entities.SubmodelJsonRequest;
import org.eclipse.tractusx.sde.common.entities.csv.CsvContent;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.common.mapper.JsonObjectMapper;
import org.eclipse.tractusx.sde.common.mapper.SubmodelMapper;
import org.eclipse.tractusx.sde.common.model.Submodel;
import org.eclipse.tractusx.sde.common.submodel.executor.SubmodelExecutor;
import org.eclipse.tractusx.sde.common.validators.SubmodelCSVValidator;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.core.failurelog.FailureLogs;
import org.eclipse.tractusx.sde.core.policy.entity.PolicyMapper;
import org.eclipse.tractusx.sde.core.policy.service.PolicyService;
import org.eclipse.tractusx.sde.core.processreport.ProcessReportUseCase;
import org.eclipse.tractusx.sde.core.processreport.model.ProcessReport;
import org.eclipse.tractusx.sde.core.submodel.executor.GenericSubmodelExecutor;
import org.eclipse.tractusx.sde.core.submodel.executor.step.DatabaseUsecaseHandler;
import org.eclipse.tractusx.sde.core.utils.SubmoduleUtility;
import org.eclipse.tractusx.sde.pcfexchange.service.impl.AsyncPushPCFDataForApproveRequest;
import org.eclipse.tractusx.sde.retrieverl.service.PolicyProvider;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmodelOrchestartorService {

	private static final String PROCESS_ID = "process_id";

	private static final String ROW_NUMBER = "row_number";

	private final SubmodelCSVValidator sumodelcsvValidator;

	private final SubmodelService submodelService;

	private final SubmodelMapper submodelMapper;

	private final JsonObjectMapper jsonObjectMapper;

	private final ProcessReportUseCase processReportUseCase;

	private final FailureLogs failureLogs;

	private final CsvHandlerService csvHandlerService;

	private final PolicyService policyService;

	private final PolicyMapper policyMapper;

	private final PolicyProvider policyProvider;

	private final AsyncPushPCFDataForApproveRequest asyncPushPCFDataForApproveRequest;

	private final SubmoduleUtility submoduleUtility;

	private final GenericSubmodelExecutor genericSubmodelExecutor;
	private final DatabaseUsecaseHandler databaseUsecaseHandler;

	ObjectMapper mapper = new ObjectMapper();

	public void processSubmodelCsv(PolicyTemplateRequest policyTemplateRequest, String processId, String submodel) {

		Submodel submodelSchemaObject = submodelService.findSubmodelByNameAsSubmdelObject(submodel);

		CsvContent csvContent = csvHandlerService.processFile(processId);
		List<String> columns = csvContent.getColumns();

		if (!sumodelcsvValidator.validate(submodelSchemaObject, columns)) {
			throw new ValidationException(String.format("Csv column header is not matching %s submodel", submodel));
		}

		PolicyModel submodelPolicyRequest = onFlyPolicyManagement(policyTemplateRequest);

		processCsv(submodelPolicyRequest, processId, submodelSchemaObject, csvContent);

	}

	public void processSubmodelAutomationCsvThroughAPI(String originalFileName, String processId) {

		CsvContent csvContent = csvHandlerService.processFile(processId);

		List<String> columns = csvContent.getColumns();

		Submodel foundSubmodelSchemaObject = findSubmodel(columns);

		PolicyModel matchingPolicyBasedOnFileName = policyProvider.getMatchingPolicyBasedOnFileName(originalFileName);

		processCsv(matchingPolicyBasedOnFileName, processId, foundSubmodelSchemaObject, csvContent);

	}

	private void processCsv(PolicyModel submodelPolicyRequest, String processId, Submodel submodelSchemaObject,
			CsvContent csvContent) {

		Runnable runnable = () -> {
			processReportUseCase.startBuildProcessReport(processId, submodelSchemaObject.getId(),
					csvContent.getRows().size(), submodelPolicyRequest.getAccessPolicies(),
					submodelPolicyRequest.getUsagePolicies(), submodelPolicyRequest.getUuid());

			AtomicInteger successCount = new AtomicInteger();
			AtomicInteger failureCount = new AtomicInteger();

			SubmodelExecutor executor = getExecutor(submodelSchemaObject.getExecutor());
			executor.init(submodelSchemaObject);

			csvContent.getRows().parallelStream().forEach(rowjObj -> {
				try {
					ObjectNode newjObject = jsonObjectMapper.submodelFileRequestToJsonNodePojo(submodelPolicyRequest);
					newjObject.put(ROW_NUMBER, rowjObj.position());
					newjObject.put(PROCESS_ID, processId);
					executor.executeCsvRecord(rowjObj, newjObject, processId, submodelPolicyRequest);
					// fetch by ID and check it if it is success then its updated.
					successCount.incrementAndGet();

				} catch (Exception e) {
					failureLogs.saveLog(processId, e.getMessage());
					failureCount.incrementAndGet();
				}
			});

			int updatedcount = executor.getUpdatedRecordCount(processId);
			successCount.set(successCount.get() - updatedcount);
			processReportUseCase.finishBuildProgressReport(processId, successCount.get(), failureCount.get(),
					updatedcount);

			// Push PCF value which already Approve request of consumer
			if (submodelSchemaObject.getId().equalsIgnoreCase("pcf")) {
				databaseUsecaseHandler.init(submodelSchemaObject.getSchema());
				List<JsonObject> readCreatedTwins = databaseUsecaseHandler.readCreatedTwins(processId, null);
				asyncPushPCFDataForApproveRequest.pushPCFDataForApproveRequest(readCreatedTwins, submodelPolicyRequest);
			}
		};

		new Thread(runnable).start();
	}

	@SneakyThrows
	public void processSubmodel(SubmodelJsonRequest submodelJsonRequest, String processId, String submodel) {
		Submodel submodelSchemaObject = submodelService.findSubmodelByNameAsSubmdelObject(submodel);
		List<ObjectNode> rowData = submodelJsonRequest.getRowData();

		PolicyModel policy = onFlyPolicyManagement(policyMapper.mapFrom(submodelJsonRequest));

		Runnable runnable = () -> {

			AtomicInteger atInt = new AtomicInteger();
			AtomicInteger successCount = new AtomicInteger();
			AtomicInteger failureCount = new AtomicInteger();
			SubmodelExecutor executor = getExecutor(submodelSchemaObject.getExecutor());
			executor.init(submodelSchemaObject);

			processReportUseCase.startBuildProcessReport(processId, submodelSchemaObject.getId(), rowData.size(),
					policy.getAccessPolicies(), policy.getUsagePolicies(), policy.getUuid());

			rowData.stream().forEach(obj -> {
				int andIncrement = atInt.incrementAndGet();
				obj.put(ROW_NUMBER, andIncrement);
				obj.put(PROCESS_ID, processId);
			});

			rowData.parallelStream().forEachOrdered(rowjObj -> {
				try {
					executor.executeJsonRecord(rowjObj.get(ROW_NUMBER).asInt(), rowjObj, processId, policy);
					successCount.incrementAndGet();
				} catch (Exception e) {
					failureLogs.saveLog(processId, e.getMessage());
					failureCount.incrementAndGet();
				}
			});

			int updatedcount = executor.getUpdatedRecordCount(processId);
			successCount.set(successCount.get() - updatedcount);
			processReportUseCase.finishBuildProgressReport(processId, successCount.get(), failureCount.get(),
					updatedcount);

			// Push PCF value which already Approve request of consumer
			if (submodelSchemaObject.getId().equalsIgnoreCase("pcf")) {
				databaseUsecaseHandler.init(submodelSchemaObject.getSchema());
				List<JsonObject> readCreatedTwins = databaseUsecaseHandler.readCreatedTwins(processId, null);
				asyncPushPCFDataForApproveRequest.pushPCFDataForApproveRequest(readCreatedTwins, policy);
			}
		};
		new Thread(runnable).start();
	}

	private SubmodelExecutor getExecutor(SubmodelExecutor executor) {
		if (executor == null)
			executor = genericSubmodelExecutor;
		return executor;
	}

	public void deleteSubmodelDigitalTwinsAndEDC(String refProcessId, String delProcessId, String submodel) {

		Submodel submodelSchema = submodelService.findSubmodelByNameAsSubmdelObject(submodel);
		AtomicInteger deletedCount = new AtomicInteger();
		AtomicInteger failureCount = new AtomicInteger();
		AtomicInteger atInt = new AtomicInteger();

		SubmodelExecutor executor = getExecutor(submodelSchema.getExecutor());
		executor.init(submodelSchema);
		ProcessReport oldProcessReport = processReportUseCase.getProcessReportById(refProcessId);

		List<JsonObject> readCreatedTwinsforDelete = executor.readCreatedTwinsforDelete(refProcessId);

		Runnable runnable = () -> {

			processReportUseCase.startDeleteProcess(oldProcessReport, refProcessId, submodel,
					readCreatedTwinsforDelete.size(), delProcessId);

			readCreatedTwinsforDelete.stream().forEach(obj -> {
				int andIncrement = atInt.incrementAndGet();
				obj.addProperty(ROW_NUMBER, andIncrement);
				obj.addProperty(PROCESS_ID, refProcessId);
			});

			readCreatedTwinsforDelete.parallelStream().forEach(rowjObj -> {
				try {
					executor.executeDeleteRecord(rowjObj.get(ROW_NUMBER).getAsInt(), rowjObj, delProcessId,
							refProcessId);
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

	public Map<Object, Object> readCreatedTwinsDetails(String submodel, String uuid, String type) {
		Submodel submodelSchema = submodelService.findSubmodelByNameAsSubmdelObject(submodel);
		SubmodelExecutor executor = getExecutor(submodelSchema.getExecutor());
		executor.init(submodelSchema);
		JsonObject readCreatedTwinsDetails = executor.readCreatedTwinsDetails(uuid);
		JsonObject jObject = new JsonObject();
		if ("csv".equalsIgnoreCase(type)) {
			List<String> csvHeader = submoduleUtility.getCSVHeader(submodelSchema);
			JsonObject jElement = readCreatedTwinsDetails.get("csv").getAsJsonObject();
			for (String field : csvHeader) {
				jObject.add(field, jElement.get(field));
			}
		} else {
			jObject = readCreatedTwinsDetails.get("json").getAsJsonObject();
		}

		return submodelMapper.jsonPojoToMap(jObject);
	}

	public void processSubmodelAutomationCsv(PolicyModel submodelFileRequest, String processId) {

		CsvContent csvContent = csvHandlerService.processFile(processId);
		List<String> columns = csvContent.getColumns();
		Submodel foundSubmodelSchemaObject = findSubmodel(columns);

		processCsv(submodelFileRequest, processId, foundSubmodelSchemaObject, csvContent);
	}

	public PolicyModel onFlyPolicyManagement(PolicyTemplateRequest policyTemplateRequest) {

		PolicyTemplateType type = policyTemplateRequest.getType();
		PolicyModel policy = policyMapper.mapFrom(policyTemplateRequest);
		switch (type) {
		case NONE:
			log.info("Nothing to do with policy");
			break;

		case EXISTING:
			PolicyModel localPolicy = policyService.getPolicy(policy.getUuid());

			if (StringUtils.isNotBlank(policy.getUuid()) && ((CollectionUtils.isNotEmpty(policy.getUsagePolicies())
					&& !localPolicy.getUsagePolicies().equals(policy.getUsagePolicies()))
					|| (CollectionUtils.isNotEmpty(policy.getAccessPolicies())
							&& !localPolicy.getAccessPolicies().equals(policy.getAccessPolicies())))) {

				if (StringUtils.isBlank(policy.getPolicyName())) {
					policy.setPolicyName(localPolicy.getPolicyName());
				}
				if (CollectionUtils.isEmpty(policy.getAccessPolicies())) {
					policy.setAccessPolicies(localPolicy.getAccessPolicies());
				}
				if (CollectionUtils.isEmpty(policy.getUsagePolicies())) {
					policy.setAccessPolicies(localPolicy.getUsagePolicies());
				}

				policy = policyService.updatePolicy(policy.getUuid(), policy);
				log.info("Updated existing policy " + policy.getUuid());

			} else {
				policy = policyService.getPolicy(policy.getUuid());
				log.info("Using existing policy for " + policy.getUuid());
			}
			break;

		case NEW:
			policy = policyService.savePolicy(policy);
			break;
		default:
			throw new ValidationException(type + " policy template type not found");
		}
		return policy;
	}

	public Submodel findSubmodel(List<String> columns) {

		Submodel foundSubmodelSchemaObject = null;
		List<Submodel> submodelDetails = submodelService.getAllSubmodels();
		for (Submodel submodel : submodelDetails) {

			if (sumodelcsvValidator.validate(submodel, columns)) {
				foundSubmodelSchemaObject = submodel;
				break;
			}
		}

		if (foundSubmodelSchemaObject == null) {
			throw new ValidationException("Csv column header is not matching with any supported submodels");
		}
		return foundSubmodelSchemaObject;
	}

}
