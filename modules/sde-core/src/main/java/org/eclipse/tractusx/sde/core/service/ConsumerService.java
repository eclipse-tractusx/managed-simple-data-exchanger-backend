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

import static org.springframework.http.ResponseEntity.ok;

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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.tractusx.sde.common.model.Submodel;
import org.eclipse.tractusx.sde.core.failurelog.mapper.DownloadHistoryMapper;
import org.eclipse.tractusx.sde.core.failurelog.repository.ConsumerDownloadHistoryRepository;
import org.eclipse.tractusx.sde.core.processreport.model.ConsumerDownloadHistory;
import org.eclipse.tractusx.sde.core.role.entity.ConsumerDownloadHistoryEntity;
import org.eclipse.tractusx.sde.core.utils.CsvUtil;
import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.edc.services.ConsumerControlPanelService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

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

	ObjectMapper mapper = new ObjectMapper();

	private DownloadHistoryMapper downloadHistoryMapper;

	@SneakyThrows
	public void subscribeAndDownloadDataOffers(ConsumerRequest consumerRequest, HttpServletResponse response) {

		ConsumerDownloadHistory consumerDownloadHistory = new ConsumerDownloadHistory();
		consumerDownloadHistory.setStartDate(LocalDateTime.now().toString());
		String processId = UUID.randomUUID().toString();

		Map<String, Object> subscribeAndDownloadDataOffers = consumerControlPanelService
				.subscribeAndDownloadDataOffers(consumerRequest);

		int failedCount = 0;
		int successCount = 0;
		consumerDownloadHistory.setConnectorId(consumerRequest.getConnectorId());
		consumerDownloadHistory.setProviderUrl(consumerRequest.getProviderUrl());
		consumerDownloadHistory.setNumberOfItems(consumerRequest.getOffers().size());
		consumerDownloadHistory.setProcessId(processId);

		Map<String, List<List<String>>> csvWithValue = new TreeMap<>();
		
		consumerRequest.getOffers().stream().forEach(offer -> {
			
			Object object = subscribeAndDownloadDataOffers.get(offer.getAssetId());
			if (object != null) {
				
				JsonNode node = mapper.convertValue(object, JsonNode.class);
				JsonNode status = node.get("status");
				JsonNode dataNode = node.get("data");
				JsonNode edrNode = node.get("edr");

				if (dataNode != null) {
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
						consumerDownloadHistory.setDownloadSuccessed(successCount + 1);
						offer.setStatus(status.asText());
					} else {
						consumerDownloadHistory.setDownloadFailed(failedCount + 1);
						offer.setStatus("FAILED");
						offer.setDownloadErrorMsg("The data does not found in csv type");
					}
				} else {
					offer.setStatus(status.asText());
					consumerDownloadHistory.setDownloadFailed(failedCount + 1);
					offer.setDownloadErrorMsg(readFieldFromJsonNode(dataNode, "error"));
				}
				
				if (edrNode != null) {
					offer.setAgreementId(readFieldFromJsonNode(edrNode, "agreementId"));
					offer.setExpirationDate(readFieldFromJsonNode(edrNode, "expirationDate"));
					offer.setTransferProcessId(readFieldFromJsonNode(edrNode, "transferProcessId"));
				}
			}
		});

		consumerDownloadHistory.setEndDate(LocalDateTime.now().toString());
		consumerDownloadHistory.setOffers(mapper.writeValueAsString(consumerRequest.getOffers()));
		consumerDownloadHistory.setPolicies(mapper.writeValueAsString(consumerRequest.getPolicies()));

		// Save consumer Download history in DB
		saveConsumerDownloadHistory(consumerDownloadHistory);

		prepareHttpResponse(response, processId, csvWithValue);
	}

	private void prepareHttpResponse(HttpServletResponse response, String processId,
			Map<String, List<List<String>>> csvWithValue) throws IOException {
		if (csvWithValue.isEmpty()) {
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("msg",
					"Unable to process your request, please try again, if error persist contact to admin");
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
		} else {
			prepareZipFiles(response, csvWithValue, processId);
		}
	}

	private String readFieldFromJsonNode(JsonNode node, String fieldName) {
		JsonNode jsonNode = node.get(fieldName);
		if (Optional.of(jsonNode).isPresent())
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

	public ResponseEntity<Object> downloadFileFromEDCUsingifAlreadyTransferStatusCompleted(List<String> assetId) {
		Map<String, Object> downloadFileFromEDCUsingifAlreadyTransferStatusCompleted = consumerControlPanelService.downloadFileFromEDCUsingifAlreadyTransferStatusCompleted(assetId);
		return ok().body(consumerControlPanelService.downloadFileFromEDCUsingifAlreadyTransferStatusCompleted(assetId));
	}

	public void saveConsumerDownloadHistory(ConsumerDownloadHistory input) {
		ConsumerDownloadHistoryEntity entity = downloadHistoryMapper.mapFrom(input);
		consumerDownloadHistoryRepository.save(entity);
	}
}
