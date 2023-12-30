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
package org.eclipse.tractusx.sde.pcfexchange.service.impl;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.bpndiscovery.handler.BpnDiscoveryProxyService;
import org.eclipse.tractusx.sde.bpndiscovery.model.request.BpnDiscoverySearchRequest;
import org.eclipse.tractusx.sde.bpndiscovery.model.request.BpnDiscoverySearchRequest.Search;
import org.eclipse.tractusx.sde.bpndiscovery.model.response.BpnDiscoveryResponse;
import org.eclipse.tractusx.sde.bpndiscovery.model.response.BpnDiscoverySearchResponse;
import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.common.mapper.JsonObjectMapper;
import org.eclipse.tractusx.sde.common.model.PagingResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellLookupRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellLookupResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelResponse;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsUtility;
import org.eclipse.tractusx.sde.digitaltwins.gateways.external.EDCDigitalTwinProxyForLookUp;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedByIdResponse;
import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.edc.model.request.Offer;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.edc.services.ConsumerControlPanelService;
import org.eclipse.tractusx.sde.edc.util.EDCAssetUrlCacheService;
import org.eclipse.tractusx.sde.pcfexchange.entity.PcfRequestEntity;
import org.eclipse.tractusx.sde.pcfexchange.entity.PcfResponseEntity;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFRequestStatusEnum;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFTypeEnum;
import org.eclipse.tractusx.sde.pcfexchange.mapper.PcfExchangeMapper;
import org.eclipse.tractusx.sde.pcfexchange.proxy.PCFExchangeProxy;
import org.eclipse.tractusx.sde.pcfexchange.repository.PcfReqsponseRepository;
import org.eclipse.tractusx.sde.pcfexchange.repository.PcfRequestRepository;
import org.eclipse.tractusx.sde.pcfexchange.request.PcfRequestModel;
import org.eclipse.tractusx.sde.pcfexchange.service.IPCFExchangeService;
import org.eclipse.tractusx.sde.submodels.pcf.service.PcfService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PcfExchangeServiceImpl implements IPCFExchangeService {

	private final PcfRequestRepository pcfRequestRepository;
	private final PcfReqsponseRepository pcfReqsponseRepository;
	private final PcfExchangeMapper pcfMapper;
	private final EDCAssetUrlCacheService edcAssetUrlCacheService;
	private final EDCDigitalTwinProxyForLookUp eDCDigitalTwinProxyForLookUp;
	private final PcfService pcfService;
	private final PCFExchangeProxy pcfExchangeProxy;
	private final DigitalTwinsUtility digitalTwinsUtility;

	private final ConsumerControlPanelService consumerControlPanelService;
	private final BpnDiscoveryProxyService bpnDiscoveryProxyService;
	private final JsonObjectMapper jsonObjectMapper;

	@Value(value = "${manufacturerId}")
	private String manufacturerId;

	@Value(value = "${digital-twins.managed.thirdparty:false}")
	private boolean managedThirdParty;

	String filterExpressionTemplate = """
			"filterExpression": [
				    {
				        "operandLeft": "https://w3id.org/edc/v0.0.1/ns/id",
				        "operator": "=",
				        "operandRight": "%s"
				    }
				]
			""";

	@Override
	public List<QueryDataOfferModel> searchPcfDataOffer(String manufacturerPartId, String searchBpnNumber) {

		List<QueryDataOfferModel> result = new ArrayList<>();
		List<String> bpnList = null;

		// 1 find bpn if empty using BPN discovery

		if (StringUtils.isBlank(searchBpnNumber)) {
			BpnDiscoverySearchRequest bpnDiscoverySearchRequest = BpnDiscoverySearchRequest.builder()
					.searchFilter(List
							.of(Search.builder().type("manufacturerPartId").keys(List.of(manufacturerPartId)).build()))
					.build();

			BpnDiscoverySearchResponse bpnDiscoverySearchData = bpnDiscoveryProxyService
					.bpnDiscoverySearchData(bpnDiscoverySearchRequest);

			bpnList = bpnDiscoverySearchData.getBpns().stream().map(BpnDiscoveryResponse::getValue).toList();

		} else {
			bpnList = List.of(searchBpnNumber);
		}

		for (String bpnNumber : bpnList) {

			// 2 fetch EDC connectors and DTR Assets from EDC connectors
			List<QueryDataOfferModel> ddTROffers = edcAssetUrlCacheService.getDDTRUrl(bpnNumber);

			// 3 lookup shell for PCF sub model
			for (QueryDataOfferModel dtOffer : ddTROffers) {

				EDRCachedByIdResponse edrToken = edcAssetUrlCacheService.verifyAndGetToken(bpnNumber, dtOffer);
				if (edrToken != null) {

					SubModelResponse lookUpPCFTwin = lookUpPCFTwin(edrToken, dtOffer, manufacturerPartId, bpnNumber);

					if (lookUpPCFTwin != null) {
						String subprotocolBody = lookUpPCFTwin.getEndpoints().get(0).getProtocolInformation()
								.getSubprotocolBody();

						String[] edcInfo = subprotocolBody.split(";");
						String[] assetInfo = edcInfo[0].split("=");
						String[] connectorInfo = edcInfo[1].split("=");

						String filterExpression = String.format(filterExpressionTemplate, assetInfo[1]);

						List<QueryDataOfferModel> queryOnDataOffers = consumerControlPanelService
								.queryOnDataOffers(connectorInfo[1], 0, 100, filterExpression);
						queryOnDataOffers.forEach(e -> e.setType("data.pcf.exchangeEndpoint"));
						result.addAll(queryOnDataOffers);
					}

				} else {
					log.warn("EDR token is null, unable to look Up PCF Twin");
				}
			}
		}
		return result;
	}

	@SneakyThrows
	@Override
	public String requestForPcfDataOffer(String productId, ConsumerRequest consumerRequest) {

		String msg = "";
		String requestId = UUID.randomUUID().toString();
		Offer offer = consumerRequest.getOffers().get(0);

		String providerBPNNumber = consumerRequest.getConnectorId();

		QueryDataOfferModel queryDataOfferModel = QueryDataOfferModel.builder().assetId(offer.getAssetId())
				.offerId(offer.getOfferId()).policyId(offer.getPolicyId()).connectorId(providerBPNNumber)
				.connectorOfferUrl(consumerRequest.getProviderUrl()).usagePolicies(consumerRequest.getPolicies())
				.build();

		EDRCachedByIdResponse edrToken = edcAssetUrlCacheService.verifyAndGetToken(providerBPNNumber,
				queryDataOfferModel);

		if (edrToken != null) {
			URI pcfEnpoint = new URI(edrToken.getEndpoint());
			Map<String, String> header = new HashMap<>();
			header.put(edrToken.getAuthKey(), edrToken.getAuthCode());

			String message = "Please provide PCF value for " + productId;

			savePcfRequestData(requestId, productId, providerBPNNumber, message, PCFTypeEnum.CONSUMER);

			// Send request to data provider for PCF value push
			pcfExchangeProxy.getPcfByProduct(pcfEnpoint, header, manufacturerId, requestId, message);

			msg = "Requested for PCF value";

		} else {
			msg = "Unable to request for PCF value becasue the EDR token status is null";
			log.warn("EDC connector " + queryDataOfferModel.getConnectorOfferUrl() + ": {},{},{}", requestId, productId,
					msg);
		}
		return msg;
	}

	@Override
	public String actionOnPcfRequestAndSendNotificationToConsumer(PcfRequestModel pcfRequestModel) {
		String msg = "";
		try {

			JsonObject calculatedPCFValue = pcfService
					.readCreatedTwinsDetailsByProductId(pcfRequestModel.getProductId()).get("json").getAsJsonObject();

			savePcfStatus(pcfRequestModel.getRequestId(), pcfRequestModel.getStatus());

			// push api call
			Runnable runnable = () -> sendNotificationToConsumer(pcfRequestModel.getStatus(), calculatedPCFValue,
					pcfRequestModel.getProductId(), pcfRequestModel.getBpnNumber(), pcfRequestModel.getRequestId());

			new Thread(runnable).start();

			msg = "PCF request '" + pcfRequestModel.getStatus()
						+ "' and asynchronously sending notification to consumer";
			
		} catch (NoDataFoundException e) {
			savePcfStatus(pcfRequestModel.getRequestId(), PCFRequestStatusEnum.FAILED);
			msg = "Unable to take action on PCF request becasue pcf calculated value does not exist, sending ERROR notification to consumer";
			throw new NoDataFoundException(msg);
		}
		return msg;
	}
	

	@Override
	public PcfRequestModel savePcfRequestData(String requestId, String productId, String bpnNumber, String message,
			PCFTypeEnum type) {

		PcfRequestModel pcfRequest = PcfRequestModel.builder()
				.requestId(requestId)
				.productId(productId)
				.bpnNumber(bpnNumber)
				.status(PCFRequestStatusEnum.REQUESTED)
				.message(message).type(type)
				.requestedTime(Instant.now().getEpochSecond())
				.lastUpdatedTime(Instant.now().getEpochSecond())
				.build();

		PcfRequestEntity pcfRequestEntity = pcfMapper.mapFrom(pcfRequest);
		return pcfMapper.mapFrom(pcfRequestRepository.save(pcfRequestEntity));
	}

	@Override
	public void recievedPCFData(String productId, String bpnNumber, String requestId, String message,
			JsonNode pcfData) {

		PCFRequestStatusEnum status = PCFRequestStatusEnum.FAILED;
		try {
			status = PCFRequestStatusEnum.valueOf(message);
		} catch (Exception e) {
			log.error("Unable to find PCF value status " + e.getMessage());
		}

		PcfResponseEntity entity = PcfResponseEntity.builder()
				.pcfData(pcfData)
				.requestId(requestId)
				.responseId(UUID.randomUUID().toString())
				.lastUpdatedTime(Instant.now().getEpochSecond())
				.build();

		pcfReqsponseRepository.save(entity);

		if (PCFRequestStatusEnum.APPROVED.equals(status)) {
			status = PCFRequestStatusEnum.RECEIVED;
		}

		savePcfStatus(requestId, status);

	}

	@Override
	public PagingResponse getPcfData(PCFRequestStatusEnum status, PCFTypeEnum type, Integer page, Integer pageSize) {

		Page<PcfRequestEntity> result = null;
		if (status == null || StringUtils.isBlank(status.toString())) {
			result = pcfRequestRepository.findByType(PageRequest.of(page, pageSize), type);
		} else {
			result = pcfRequestRepository.findByTypeAndStatus(PageRequest.of(page, pageSize), type, status);
		}

		List<PcfRequestModel> requestList = result.stream().map(pcfMapper::mapFrom).toList();

		return PagingResponse.builder().items(requestList).pageSize(result.getSize()).page(result.getNumber())
				.totalItems(result.getTotalElements()).build();
	}


	@SneakyThrows
	private void sendNotificationToConsumer(PCFRequestStatusEnum status, JsonObject calculatedPCFValue, String productId, String bpnNumber,
			String requestId) {
		String sendNotificationStatus = "";
		try {


			if (PCFRequestStatusEnum.APPROVED.equals(status)
					|| PCFRequestStatusEnum.PUSHING_DATA_FAILED.equals(status)) {
				savePcfStatus(requestId, PCFRequestStatusEnum.PUSHING_DATA);
			} else if (PCFRequestStatusEnum.REJECTED.equals(status)
					|| PCFRequestStatusEnum.SENDING_REJECTING_NOTIFICATION_FAILED.equals(status)) {
				calculatedPCFValue = null;
				savePcfStatus(requestId, PCFRequestStatusEnum.SENDING_REJECTING_NOTIFICATION);
			}
			
			// 1 fetch EDC connectors and DTR Assets from EDC connectors
			List<QueryDataOfferModel> pcfExchangeUrlOffers = edcAssetUrlCacheService
					.getPCFExchangeUrlFromTwin(bpnNumber);

			String message = status.name();

			// 2 lookup shell for PCF sub model
			for (QueryDataOfferModel dtOffer : pcfExchangeUrlOffers) {

				EDRCachedByIdResponse edrToken = edcAssetUrlCacheService.verifyAndGetToken(bpnNumber, dtOffer);

				if (edrToken != null) {
					URI pcfpushEnpoint = new URI(edrToken.getEndpoint());
					Map<String, String> header = new HashMap<>();
					header.put(edrToken.getAuthKey(), edrToken.getAuthCode());

					pcfExchangeProxy.uploadPcfSubmodel(pcfpushEnpoint, header, productId, bpnNumber, requestId, message,
							jsonObjectMapper.gsonObjectToJsonNode(calculatedPCFValue));

					sendNotificationStatus = "SUCCESS";
				} else {
					log.warn("EDC connector " + dtOffer.getConnectorOfferUrl()
							+ ", The EDR token is null to find pcf exchange asset");
				}
			}
		} catch (FeignException e) {
			log.error("FeignRequest:" +e.request());
			String errorMsg = "Unable to send notification to consumer because: " + e.contentUTF8();
			log.error("FeignException : " + errorMsg);
		} finally {
			
			updatePCFPushStatus(status, requestId, sendNotificationStatus);
		}
	}

	private void updatePCFPushStatus(PCFRequestStatusEnum status, String requestId, String sendNotificationStatus) {
		if (PCFRequestStatusEnum.APPROVED.equals(status) && StringUtils.isNotBlank(sendNotificationStatus)) {
			savePcfStatus(requestId, PCFRequestStatusEnum.PUSHED);
		} else if (PCFRequestStatusEnum.REJECTED.equals(status) && StringUtils.isNotBlank(sendNotificationStatus)) {
			savePcfStatus(requestId, PCFRequestStatusEnum.REJECTED);
		} else if (PCFRequestStatusEnum.APPROVED.equals(status)
				|| PCFRequestStatusEnum.PUSHING_DATA_FAILED.equals(status)) {
			savePcfStatus(requestId, PCFRequestStatusEnum.PUSHING_DATA_FAILED);
		} else if (PCFRequestStatusEnum.REJECTED.equals(status)
				|| PCFRequestStatusEnum.SENDING_REJECTING_NOTIFICATION_FAILED.equals(status))
			savePcfStatus(requestId, PCFRequestStatusEnum.SENDING_REJECTING_NOTIFICATION_FAILED);
		else {
			savePcfStatus(requestId, PCFRequestStatusEnum.FAILED);
		}
	}

	@SneakyThrows
	private PcfRequestEntity savePcfStatus(String requestId, PCFRequestStatusEnum status) {

		PcfRequestEntity pcfRequestEntity = pcfRequestRepository.getReferenceById(requestId);
		pcfRequestEntity.setLastUpdatedTime(Instant.now().getEpochSecond());
		pcfRequestEntity.setStatus(status);
		log.info("'" + pcfRequestEntity.getProductId() + "' pcf request saved in the database successfully as {}",
				status);
		pcfRequestRepository.save(pcfRequestEntity);
		return pcfRequestEntity;

	}

	private ShellLookupRequest getShellLookupRequest(String manufacturerPartId, String bpnNumber) {

		ShellLookupRequest shellLookupRequest = new ShellLookupRequest();
		getSpecificAssetIds(manufacturerPartId, bpnNumber).entrySet().stream()
				.forEach(entry -> shellLookupRequest.addLocalIdentifier(entry.getKey(), entry.getValue()));

		return shellLookupRequest;
	}

	private Map<String, String> getSpecificAssetIds(String manufacturerPartId, String bpnNumber) {
		Map<String, String> specificIdentifiers = new HashMap<>();
		specificIdentifiers.put(CommonConstants.MANUFACTURER_PART_ID, manufacturerPartId);
		specificIdentifiers.put(CommonConstants.MANUFACTURER_ID, bpnNumber);
		specificIdentifiers.put(CommonConstants.ASSET_LIFECYCLE_PHASE, CommonConstants.AS_PLANNED);

		return specificIdentifiers;
	}

	@SneakyThrows
	private SubModelResponse lookUpPCFTwin(EDRCachedByIdResponse edrToken, QueryDataOfferModel dtOffer,
			String manufacturerPartId, String bpnNumber) {

		String endpoint = edrToken.getEndpoint();
		String dtOfferUrl = dtOffer.getConnectorOfferUrl();
		ShellLookupRequest shellLookupRequest = getShellLookupRequest(manufacturerPartId, bpnNumber);
		try {
			Map<String, String> header = Map.of(edrToken.getAuthKey(), edrToken.getAuthCode());

			String assetIds = managedThirdParty
					? digitalTwinsUtility.encodeAssetIdsObject(shellLookupRequest.toJsonString())
					: shellLookupRequest.toJsonString();

			ResponseEntity<ShellLookupResponse> shellLookup = eDCDigitalTwinProxyForLookUp
					.shellLookup(new URI(endpoint), assetIds, header);
			ShellLookupResponse body = shellLookup.getBody();

			if (shellLookup.getStatusCode() == HttpStatus.OK && body != null) {
				return getPCFSubmodelDetails(shellLookupRequest, endpoint, header, dtOfferUrl, body.getResult());
			}

		} catch (FeignException e) {
			String errorMsg = "Unable to look up PCF twin " + dtOfferUrl + ", " + shellLookupRequest.toJsonString()
					+ " because: " + e.contentUTF8();
			log.error("FeignException : " + errorMsg);
		} catch (Exception e) {
			String errorMsg = "Unable to look up PCF twin " + dtOfferUrl + ", " + shellLookupRequest.toJsonString()
					+ "because: " + e.getMessage();
			log.error("Exception : " + errorMsg);
		}

		return null;
	}

	@SneakyThrows
	private SubModelResponse getPCFSubmodelDetails(ShellLookupRequest shellLookupRequest, String endpoint,
			Map<String, String> header, String dtOfferUrl, List<String> shellIds) {

		SubModelResponse subModelResponseRes = null;
		if (shellIds == null) {
			log.warn(dtOfferUrl + ", No pcf aspect found for " + shellLookupRequest.toJsonString());
		} else if (shellIds.size() > 1) {
			log.warn(String.format("Multiple shell id's found for pcfAspect %s, %s", dtOfferUrl,
					shellLookupRequest.toJsonString()));
		} else if (shellIds.size() == 1) {
			ResponseEntity<ShellDescriptorResponse> shellDescriptorResponse = eDCDigitalTwinProxyForLookUp
					.getShellDescriptorByShellId(new URI(endpoint),
							digitalTwinsUtility.encodeShellIdBase64Utf8(shellIds.get(0)), header);
			ShellDescriptorResponse shellDescriptorResponseBody = shellDescriptorResponse.getBody();
			if (shellDescriptorResponse.getStatusCode() == HttpStatus.OK && shellDescriptorResponseBody != null) {
				for (SubModelResponse subModelResponse : shellDescriptorResponseBody.getSubmodelDescriptors()) {
					if (!subModelResponse.getIdShort().isEmpty() && subModelResponse.getIdShort().contains("pcf")) {
						subModelResponseRes = subModelResponse;
					}
				}
			}
		}
		return subModelResponseRes;
	}

	@Override
	public PcfResponseEntity viewForPcfDataOffer(String requestId) {
			Optional<PcfResponseEntity> findById = pcfReqsponseRepository.findFirstByRequestIdOrderByLastUpdatedTimeDesc(requestId);
			if (!findById.isPresent())
				throw new NoDataFoundException("No data found uuid " + requestId);
			return findById.get();
	}

}
