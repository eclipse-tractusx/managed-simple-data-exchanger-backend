/********************************************************************************
 * Copyright (c) 2023 T-Systems International GmbH
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.tractusx.sde.common.enums.ProgressStatusEnum;
import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.common.model.Submodel;
import org.eclipse.tractusx.sde.core.failurelog.repository.ConsumerDownloadHistoryRepository;
import org.eclipse.tractusx.sde.core.processreport.entity.ConsumerDownloadHistoryEntity;
import org.eclipse.tractusx.sde.core.processreport.mapper.ConsumerDownloadHistoryMapper;
import org.eclipse.tractusx.sde.core.processreport.model.ConsumerDownloadHistory;
import org.eclipse.tractusx.sde.core.utils.CsvUtil;
import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.edc.model.request.Offer;
import org.eclipse.tractusx.sde.edc.services.ConsumerControlPanelService;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Service
@AllArgsConstructor
public class ConsumerService {

	private final ConsumerControlPanelService consumerControlPanelService;

	private final SubmodelOrchestartorService submodelOrchestartorService;

	private final ConsumerDownloadHistoryRepository consumerDownloadHistoryRepository;

	private final ConsumerDownloadHistoryMapper consumerDownloadHistoryMapper;

	
	ObjectMapper mapper = new ObjectMapper();

	@SneakyThrows
	public void subscribeAndDownloadDataOffers(ConsumerRequest consumerRequest, HttpServletResponse response) {

		String processId = UUID.randomUUID().toString();
		AtomicInteger failedCount = new AtomicInteger();
		AtomicInteger successCount = new AtomicInteger();
		String startDate = LocalDateTime.now().toString();
		
		ConsumerDownloadHistoryEntity entity = ConsumerDownloadHistoryEntity.builder()
				.startDate(startDate)
				.endDate(LocalDateTime.now().toString())
				.connectorId(consumerRequest.getConnectorId())
				.providerUrl(consumerRequest.getProviderUrl())
				.numberOfItems(consumerRequest.getOffers().size())
				.downloadSuccessed(successCount.get())
				.downloadFailed(failedCount.get())
				.processId(processId)
				.status(ProgressStatusEnum.IN_PROGRESS.toString())
				.build();
		
		// Save consumer Download history in DB
		consumerDownloadHistoryRepository.save(entity);
				
		Map<String, Object> subscribeAndDownloadDataOffers = consumerControlPanelService
				.subscribeAndDownloadDataOffers(consumerRequest);


		Map<String, List<List<String>>> csvWithValue = new TreeMap<>();

		consumerRequest.getOffers().stream().forEach(offer -> prepareFromOfferResponse(subscribeAndDownloadDataOffers,
				failedCount, successCount, csvWithValue, offer));

		
		entity.setEndDate(LocalDateTime.now().toString());
		entity.setOffers(mapper.writeValueAsString(consumerRequest.getOffers()));
		entity.setPolicies(mapper.writeValueAsString(consumerRequest.getPolicies()));
		entity.setDownloadSuccessed(successCount.get());
		entity.setDownloadFailed(failedCount.get());
		
		entity.setStatus(ProgressStatusEnum.FAILED.toString());
		if (consumerRequest.getOffers().size() == successCount.get())
			entity.setStatus(ProgressStatusEnum.COMPLETED.toString());
		else if(successCount.get()!=0 && failedCount.get()!=0 )
			entity.setStatus(ProgressStatusEnum.PARTIALED_FAILED.toString());

		// Save consumer Download history in DB
		consumerDownloadHistoryRepository.save(entity);

		prepareHttpResponse(response, processId, csvWithValue);
	}

	@SneakyThrows
	public void downloadFileFromEDCUsingifAlreadyTransferStatusCompleted(String referenceProcessId,
			HttpServletResponse response) {
		
		String processId = UUID.randomUUID().toString();
		
		ConsumerDownloadHistoryEntity entity = consumerDownloadHistoryRepository.findByProcessId(referenceProcessId);
		
		if (entity!=null && entity.getOffers() != null) {

			List<Offer> offerList = mapper.readValue(entity.getOffers(), new TypeReference<List<Offer>>() {
			});

			if (!offerList.isEmpty()) {
				AtomicInteger failedCount = new AtomicInteger();
				AtomicInteger successCount = new AtomicInteger();

				entity.setStartDate(LocalDateTime.now().toString());
				entity.setNumberOfItems(offerList.size());
				entity.setDownloadSuccessed(successCount.get());
				entity.setDownloadFailed(failedCount.get());
				entity.setProcessId(processId);
				entity.setStatus(ProgressStatusEnum.IN_PROGRESS.toString());
				entity.setReferenceProcessId(referenceProcessId);

				// Save consumer Download history in DB
				consumerDownloadHistoryRepository.save(entity);

				Map<String, List<List<String>>> csvWithValue = new TreeMap<>();

				List<String> assetIds = offerList.stream().map(Offer::getAssetId).toList();

				Map<String, Object> downloadFileFromEDCUsingifAlreadyTransferStatusCompleted = consumerControlPanelService
						.downloadFileFromEDCUsingifAlreadyTransferStatusCompleted(assetIds);

				offerList.stream()
						.forEach(offer -> prepareFromOfferResponse(
								downloadFileFromEDCUsingifAlreadyTransferStatusCompleted, failedCount, successCount,
								csvWithValue, offer));

				entity.setEndDate(LocalDateTime.now().toString());
				entity.setOffers(mapper.writeValueAsString(offerList));
				entity.setDownloadSuccessed(successCount.get());
				entity.setDownloadFailed(failedCount.get());
				entity.setProcessId(processId);
				entity.setReferenceProcessId(referenceProcessId);
				
				entity.setStatus(ProgressStatusEnum.FAILED.toString());
				if (offerList.size() == successCount.get())
					entity.setStatus(ProgressStatusEnum.COMPLETED.toString());
				else if(successCount.get()!=0 && failedCount.get()!=0 )
					entity.setStatus(ProgressStatusEnum.PARTIALED_FAILED.toString());

				// Save consumer Download history in DB
				consumerDownloadHistoryRepository.save(entity);

				prepareHttpResponse(response, processId, csvWithValue);
			} else {
				generateFailureJsonResponse(response, "Unable to find data offer in SDE for redownload");
			}
		} else {
			generateFailureJsonResponse(response, "Unable to find data offer in SDE for redownload");
		}
	}

	private void prepareFromOfferResponse(Map<String, Object> subscribeAndDownloadDataOffers, AtomicInteger failedCount,
			AtomicInteger successCount, Map<String, List<List<String>>> csvWithValue, Offer offer) {
		Object object = subscribeAndDownloadDataOffers.get(offer.getAssetId());
		if (object != null) {

			JsonNode node = mapper.convertValue(object, JsonNode.class);
			JsonNode status = node.get("status");
			JsonNode dataNode = node.get("data");
			JsonNode edrNode = node.get("edr");

			if (dataNode != null) {
				processCSVDataObject(successCount, failedCount, csvWithValue, offer, status, dataNode);
			} else {
				offer.setStatus(status.asText());
				failedCount.getAndIncrement();
				offer.setDownloadErrorMsg(readFieldFromJsonNode(node, "error"));
			}

			if (edrNode != null) {
				offer.setAgreementId(readFieldFromJsonNode(edrNode, "agreementId"));
				offer.setExpirationDate(readFieldFromJsonNode(edrNode, "expirationDate"));
				offer.setTransferProcessId(readFieldFromJsonNode(edrNode, "transferProcessId"));
			}
		}
	}

	private void processCSVDataObject(AtomicInteger successCount, AtomicInteger failedCount,
			Map<String, List<List<String>>> csvWithValue, Offer offer, JsonNode status, JsonNode dataNode) {

		JsonNode csvNode = dataNode.get("csv");
		if (csvNode != null) {
			List<String> csvHeader = new ArrayList<>();
			List<String> csvValues = new ArrayList<>();

			csvNode.fieldNames().forEachRemaining(csvHeader::add);
			csvNode.fields().forEachRemaining(obje -> csvValues.add(obje.getValue().asText()));

			Submodel findSubmodel = submodelOrchestartorService.findSubmodel(csvHeader);
			List<List<String>> csvValueList = csvWithValue.get(findSubmodel.getId());
			if (csvValueList == null) {
				csvValueList = new ArrayList<>();
				csvValueList.add(csvHeader);
			}

			csvValueList.add(csvValues);
			csvWithValue.put(findSubmodel.getId(), csvValueList);
			offer.setStatus(status.asText());
			successCount.getAndIncrement();
		} else {
			offer.setStatus("FAILED");
			offer.setDownloadErrorMsg("The csv type data does not found in response");
			failedCount.getAndIncrement();
		}
	}

	private void prepareHttpResponse(HttpServletResponse response, String processId,
			Map<String, List<List<String>>> csvWithValue) throws IOException {
		if (csvWithValue.isEmpty()) {
			generateFailureJsonResponse(response, "Unable to process your request, please try again, if error persist contact to admin");
		} else {
			prepareZipFiles(response, csvWithValue, processId);
		}
	}

	private void generateFailureJsonResponse(HttpServletResponse response, String errormsg) throws IOException {
		Map<String, String> errorResponse = new HashMap<>();
		errorResponse.put("msg", errormsg);
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		response.setCharacterEncoding("UTF-8");
		String jsonData = new Gson().toJson(errorResponse);
		PrintWriter out = response.getWriter();
		try {
			out.print(jsonData);
		} finally {
			out.close();
		}
	}

	private String readFieldFromJsonNode(JsonNode node, String fieldName) {
		JsonNode jsonNode = node.get(fieldName);
		if (Optional.ofNullable(jsonNode).isPresent())
			return jsonNode.asText();
		else
			return null;
	}

	@SneakyThrows
	private void prepareZipFiles(HttpServletResponse response, Map<String, List<List<String>>> csvWithValue,
			String processId) {

		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment;filename=" + processId + "-download.zip");
		response.setStatus(HttpServletResponse.SC_OK);

		try (ZipOutputStream zippedOut = new ZipOutputStream(response.getOutputStream())) {
			for (Entry<String, List<List<String>>> entry : csvWithValue.entrySet()) {
				String fileName = entry.getKey();
				List<List<String>> value = entry.getValue();

				ByteArrayInputStream file = CsvUtil.writeCsv(value);
				ZipEntry e = new ZipEntry(fileName + ".csv");
				// Configure the zip entry, the properties of the file
				e.setSize(file.read());

				e.setTime(System.currentTimeMillis());
				// etc.
				zippedOut.putNextEntry(e);
				// And the content of the resource:
				StreamUtils.copy(file, zippedOut);

				zippedOut.closeEntry();
			}
			zippedOut.finish();
		} catch (Exception e) {
			e.getStackTrace();
		}
	}

	public List<ConsumerDownloadHistory> viewDownloadHistory() {
		return consumerDownloadHistoryRepository.findAll().stream()
				.map(consumerDownloadHistoryMapper::mapFromCustom).toList();
	}

	public ConsumerDownloadHistory viewConsumerDownloadHistoryDetails(String processId) {
		ConsumerDownloadHistoryEntity entity = Optional
				.ofNullable(consumerDownloadHistoryRepository.findByProcessId(processId))
				.orElseThrow(() -> new NoDataFoundException("No data found processId " + processId));
		return consumerDownloadHistoryMapper.mapFromCustom(entity);
	}

}
