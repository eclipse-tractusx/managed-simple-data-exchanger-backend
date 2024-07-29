/********************************************************************************
 * Copyright (c) 2024 T-Systems International GmbH
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

import static org.eclipse.tractusx.sde.common.enums.ProgressStatusEnum.COMPLETED;
import static org.eclipse.tractusx.sde.common.enums.ProgressStatusEnum.FAILED;
import static org.eclipse.tractusx.sde.common.enums.ProgressStatusEnum.PARTIALLY_FAILED;

import java.io.IOException;
import java.io.OutputStreamWriter;
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
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.enums.ProgressStatusEnum;
import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.common.model.Acknowledgement;
import org.eclipse.tractusx.sde.common.model.PagingResponse;
import org.eclipse.tractusx.sde.common.model.Submodel;
import org.eclipse.tractusx.sde.core.processreport.entity.ConsumerDownloadHistoryEntity;
import org.eclipse.tractusx.sde.core.processreport.mapper.ConsumerDownloadHistoryMapper;
import org.eclipse.tractusx.sde.core.processreport.model.ConsumerDownloadHistory;
import org.eclipse.tractusx.sde.core.processreport.repository.ConsumerDownloadHistoryRepository;
import org.eclipse.tractusx.sde.edc.constants.EDCAssetConstant;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ActionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyConstraintBuilderService;
import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.edc.model.request.Offer;
import org.eclipse.tractusx.sde.edc.services.ConsumerControlPanelService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;

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

	private final PolicyConstraintBuilderService policyConstraintBuilderService;

	ObjectMapper mapper = new ObjectMapper();

	public Acknowledgement subscribeAndDownloadDataOffersAsync(ConsumerRequest consumerRequest) {
		String processId = UUID.randomUUID().toString();

		Runnable runnable = () -> subscribeAndDownloadDataOffers(consumerRequest, processId, false);
		new Thread(runnable).start();

		return Acknowledgement.builder().id(processId).build();
	}

	@SneakyThrows
	public void subscribeAndDownloadDataOffersSynchronous(ConsumerRequest consumerRequest,
			HttpServletResponse response) {
		String processId = UUID.randomUUID().toString();
		prepareHttpResponse(response, processId, subscribeAndDownloadDataOffers(consumerRequest, processId, true),
				consumerRequest.getDownloadDataAs());
	}

	@SneakyThrows
	public Map<String, Object> subscribeAndDownloadDataOffers(ConsumerRequest consumerRequest, String processId,
			boolean flagToDownloadImidiate) {

		AtomicInteger failedCount = new AtomicInteger();
		AtomicInteger successCount = new AtomicInteger();

		Map<String, Object> dataWithValue = new TreeMap<>();

		Map<Object, List<Offer>> collect = consumerRequest.getOffers().stream().collect(Collectors.groupingBy(
				ele -> StringUtils.join(ele.getConnectorId(), "_", ele.getConnectorOfferUrl()), Collectors.toList()));

		collect.entrySet().parallelStream().forEach(entry -> {

			String key = entry.getKey().toString();
			String[] strs = key.split("_");

			ConsumerDownloadHistoryEntity entity = ConsumerDownloadHistoryEntity.builder()
					.startDate(LocalDateTime.now()).connectorId(strs[0]).providerUrl(strs[1])
					.numberOfItems(consumerRequest.getOffers().size()).downloadSuccessed(successCount.get())
					.downloadFailed(failedCount.get()).processId(processId)
					.status(ProgressStatusEnum.IN_PROGRESS.toString()).build();

			// Save consumer Download history in DB
			consumerDownloadHistoryRepository.save(entity);

			List<ActionRequest> action = policyConstraintBuilderService
					.getUsagePoliciesConstraints(consumerRequest.getUsagePolicies());

			entry.getValue().parallelStream().forEach(offer -> {

				Object subcribeAndDownloadOffer = consumerControlPanelService.subcribeAndDownloadOffer(offer, action,
						flagToDownloadImidiate, consumerRequest.getDownloadDataAs());

				prepareFromOfferResponse(subcribeAndDownloadOffer, failedCount, successCount, dataWithValue, offer,
						flagToDownloadImidiate, consumerRequest.getDownloadDataAs());

			});

			entity.setEndDate(LocalDateTime.now());
			try {
				entity.setOffers(mapper.writeValueAsString(entry.getValue()));
				entity.setPolicies(mapper.writeValueAsString(consumerRequest.getUsagePolicies()));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

			entity.setDownloadSuccessed(successCount.get());
			entity.setDownloadFailed(failedCount.get());

			entity.setStatus(ProgressStatusEnum.FAILED.toString());
			if (consumerRequest.getOffers().size() == successCount.get())
				entity.setStatus(ProgressStatusEnum.COMPLETED.toString());
			else if (successCount.get() != 0 && failedCount.get() != 0)
				entity.setStatus(ProgressStatusEnum.PARTIALLY_FAILED.toString());

			// Save consumer Download history in DB
			consumerDownloadHistoryRepository.save(entity);

		});

		return dataWithValue;
	}

	@SneakyThrows
	public void downloadFileFromEDCUsingifAlreadyTransferStatusCompleted(String referenceProcessId, String type,
			HttpServletResponse response) {

		String processId = UUID.randomUUID().toString();

		ConsumerDownloadHistoryEntity entity = consumerDownloadHistoryRepository.findByProcessId(referenceProcessId);

		if (entity != null && entity.getOffers() != null) {

			List<Offer> offerList = mapper.readValue(entity.getOffers(), new TypeReference<List<Offer>>() {
			});

			if (!offerList.isEmpty()) {
				AtomicInteger failedCount = new AtomicInteger();
				AtomicInteger successCount = new AtomicInteger();

				entity.setStartDate(LocalDateTime.now());
				entity.setNumberOfItems(offerList.size());
				entity.setDownloadSuccessed(successCount.get());
				entity.setDownloadFailed(failedCount.get());
				entity.setProcessId(processId);
				entity.setStatus(ProgressStatusEnum.IN_PROGRESS.toString());
				entity.setReferenceProcessId(referenceProcessId);

				// Save consumer Download history in DB
				consumerDownloadHistoryRepository.save(entity);

				Map<String, Object> dataWithValue = new TreeMap<>();

				List<String> assetIds = offerList.stream().map(Offer::getAssetId).toList();

				Map<String, Object> downloadFileFromEDCUsingifAlreadyTransferStatusCompleted = consumerControlPanelService
						.downloadFileFromEDCUsingifAlreadyTransferStatusCompleted(assetIds, type);

				offerList.stream()
						.forEach(offer -> prepareFromOfferResponse(
								downloadFileFromEDCUsingifAlreadyTransferStatusCompleted.get(offer.getAssetId()),
								failedCount, successCount, dataWithValue, offer, true, type));

				entity.setEndDate(LocalDateTime.now());
				entity.setOffers(mapper.writeValueAsString(offerList));
				entity.setDownloadSuccessed(successCount.get());
				entity.setDownloadFailed(failedCount.get());
				entity.setProcessId(processId);
				entity.setReferenceProcessId(referenceProcessId);

				entity.setStatus(FAILED.toString());
				if (offerList.size() == successCount.get())
					entity.setStatus(COMPLETED.toString());
				else if (successCount.get() != 0 && failedCount.get() != 0)
					entity.setStatus(PARTIALLY_FAILED.toString());

				// Save consumer Download history in DB
				consumerDownloadHistoryRepository.save(entity);

				prepareHttpResponse(response, processId, dataWithValue, type);
			} else {
				generateFailureJsonResponse(response, "Unable to find data offer in SDE for download");
			}
		} else {
			generateFailureJsonResponse(response, "Unable to find data offer in SDE for download");
		}
	}

	private void prepareFromOfferResponse(Object object, AtomicInteger failedCount, AtomicInteger successCount,
			Map<String, Object> dataWithValue, Offer offer, boolean flagToDownloadImidiate, String downloadDataAs) {

		if (object != null) {

			JsonNode node = mapper.convertValue(object, JsonNode.class);
			JsonNode status = node.get("status");
			JsonNode dataNode = node.get("data");
			JsonNode edrNode = node.get("edr");

			if (dataNode != null && flagToDownloadImidiate) {
				if ("csv".equalsIgnoreCase(downloadDataAs))
					processCSVDataObject(successCount, failedCount, dataWithValue, offer, status, dataNode);
				else
					processJsonDataObject(successCount, failedCount, dataWithValue, offer, status, dataNode);
			} else if (!flagToDownloadImidiate && "SUCCESS".equals(status.asText())) {
				offer.setStatus("SUCCESS");
				successCount.getAndIncrement();
				offer.setDownloadErrorMsg("");
			} else {
				offer.setStatus(FAILED.toString());
				failedCount.getAndIncrement();
				offer.setDownloadErrorMsg(readFieldFromJsonNode(node, "error"));
			}

			if (edrNode != null) {
				offer.setAgreementId(readFieldFromJsonNode(edrNode, EDCAssetConstant.ASSET_PREFIX + "agreementId"));
				offer.setExpirationDate(readFieldFromJsonNode(edrNode, "tx:expirationDate"));
				offer.setTransferProcessId(
						readFieldFromJsonNode(edrNode, EDCAssetConstant.ASSET_PREFIX + "transferProcessId"));
			}
		}
	}

	private void processJsonDataObject(AtomicInteger successCount, AtomicInteger failedCount,
			Map<String, Object> dataWithValue, Offer offer, JsonNode status, JsonNode jsonNode) {
		if (jsonNode != null) {
			dataWithValue.put(offer.getAssetId(), jsonNode);
			offer.setStatus(status.asText());
			successCount.getAndIncrement();
			offer.setDownloadErrorMsg("");
		} else {
			offer.setStatus(FAILED.toString());
			offer.setDownloadErrorMsg("The json type data does not found in response");
			failedCount.getAndIncrement();
		}
	}

	@SuppressWarnings("unchecked")
	private void processCSVDataObject(AtomicInteger successCount, AtomicInteger failedCount,
			Map<String, Object> dataWithValue, Offer offer, JsonNode status, JsonNode csvNode) {

		if (csvNode != null) {
			List<String> csvHeader = new ArrayList<>();
			List<String> csvValues = new ArrayList<>();

			csvNode.fieldNames().forEachRemaining(csvHeader::add);
			csvNode.fields().forEachRemaining(obje -> csvValues.add(obje.getValue().asText()));

			Submodel findSubmodel = submodelOrchestartorService.findSubmodel(csvHeader);
			Object obj = dataWithValue.get(findSubmodel.getId());

			List<Object> csvValueList = null;

			if (obj == null) {
				csvValueList = new ArrayList<>();
				csvValueList.add(csvHeader);
			} else {
				csvValueList = (ArrayList<Object>) obj;
			}

			csvValueList.add(csvValues);
			dataWithValue.put(findSubmodel.getId(), csvValueList);
			offer.setStatus(status.asText());
			successCount.getAndIncrement();
			offer.setDownloadErrorMsg("");
		} else {
			offer.setStatus(FAILED.toString());
			offer.setDownloadErrorMsg("The csv type data does not found in response");
			failedCount.getAndIncrement();
		}
	}

	private void prepareHttpResponse(HttpServletResponse response, String processId, Map<String, Object> csvWithValue,
			String typeAsDownload) throws IOException {
		if (csvWithValue.isEmpty()) {
			generateFailureJsonResponse(response, "Unable to process your request, please try again");
		} else {
			prepareZipFiles(response, csvWithValue, processId, typeAsDownload);
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
	private void prepareZipFiles(HttpServletResponse response, Map<String, Object> csvWithValue, String processId,
			String downloadDataAs) {

		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment;filename=" + processId + "-download.zip");
		response.setStatus(HttpServletResponse.SC_OK);
		if ("csv".equalsIgnoreCase(downloadDataAs))
			writeCSVFiles(response, csvWithValue);
		else
			writeJsonFiles(response, csvWithValue);
	}

	@SneakyThrows
	private void writeJsonFiles(HttpServletResponse response, Map<String, Object> csvWithValue) {
		try (ZipOutputStream zip = new ZipOutputStream(response.getOutputStream())) {
			for (Entry<String, Object> entry : csvWithValue.entrySet()) {
				String fileName = entry.getKey();
				fileName = fileName.replace(":", "-");
				Object value = entry.getValue();
				zip.putNextEntry(new ZipEntry(fileName + ".json"));
				Object json = mapper.readValue(value.toString(), Object.class);
				String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
				zip.write(jsonStr.getBytes());
				zip.closeEntry();
			}
			zip.finish();
		} catch (Exception e) {
			e.getStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	private void writeCSVFiles(HttpServletResponse response, Map<String, Object> csvWithValue) {
		try (ZipOutputStream zippedOut = new ZipOutputStream(response.getOutputStream())) {
			for (Entry<String, Object> entry : csvWithValue.entrySet()) {
				String fileName = entry.getKey();
				Object value = entry.getValue();

				// create a zip entry and add it to ZipOutputStream
				ZipEntry e = new ZipEntry(fileName + ".csv");
				zippedOut.putNextEntry(e);
				// There is no need for staging the CSV on filesystem or reading bytes into
				// memory. Directly write bytes to the output stream.
				CSVWriter writer = new CSVWriter(new OutputStreamWriter(zippedOut),
						';', ICSVWriter.NO_QUOTE_CHARACTER,
						'/', ICSVWriter.DEFAULT_LINE_END);

				List<Object> valueList = (ArrayList<Object>) value;
				for (Object list : valueList) {

					List<String> valArray = (ArrayList<String>) list;
					String[] strarray = new String[valArray.size()];
					valArray.toArray(strarray);
					// write the contents
					writer.writeNext(strarray, false);
				}
				// flush the writer. Very important!
				writer.flush();
				// close the entry. Note : we are not closing the zos just yet as we need to add
				// more files to our ZIP
				zippedOut.closeEntry();
			}
			zippedOut.finish();
		} catch (Exception e) {
			e.getStackTrace();
		}
	}

	public PagingResponse viewDownloadHistory(Integer page, Integer pageSize) {
		Page<ConsumerDownloadHistoryEntity> result = consumerDownloadHistoryRepository
				.findAll(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "endDate")));
		List<ConsumerDownloadHistory> processReports = result.stream().map(consumerDownloadHistoryMapper::mapFromCustom)
				.toList();
		return PagingResponse.builder().items(processReports).pageSize(result.getSize()).page(result.getNumber())
				.totalItems(result.getTotalElements()).build();

	}

	public ConsumerDownloadHistory viewConsumerDownloadHistoryDetails(String processId) {
		ConsumerDownloadHistoryEntity entity = Optional
				.ofNullable(consumerDownloadHistoryRepository.findByProcessId(processId))
				.orElseThrow(() -> new NoDataFoundException("No data found processId " + processId));
		return consumerDownloadHistoryMapper.mapFromCustom(entity);
	}

}