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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
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
import org.eclipse.tractusx.sde.common.model.PagingResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellLookupRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellLookupResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelResponse;
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

	private final ConsumerControlPanelService consumerControlPanelService;
	private final BpnDiscoveryProxyService bpnDiscoveryProxyService;

	@Value(value = "${manufacturerId}")
	private String manufacturerId;

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
							.of(Search.builder()
									.type("manufacturerPartId")
									.keys(List.of(manufacturerPartId))
									.build()))
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
						
						List<QueryDataOfferModel> queryOnDataOffers = consumerControlPanelService.queryOnDataOffers(connectorInfo[1], 0, 100, filterExpression);
						queryOnDataOffers.forEach(e-> e.setType("data.pcf.exchangeEndpoint"));	
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
			
			savePcfRequestData(requestId, productId, manufacturerId, message, PCFTypeEnum.CONSUMER);

			//Send request to data provider for PCF value push
			pcfExchangeProxy.getPcfByProduct(pcfEnpoint, header, productId, manufacturerId, requestId, message);


		} else {
			log.warn("EDC connector " + queryDataOfferModel.getConnectorOfferUrl()
					+ ", The EDR token is null to find pcf exchange asset");
		}
		return "Requested for PCF value";
	}

	@Override
	public void approveAndPushPCFData(String productId, String bpnNumber, String requestId, String message) {

		JsonObject calculatedPCFValue = pcfService.readCreatedTwinsDetailsByProductId(productId);

		Optional<PcfRequestEntity> pcfRequestData = getPCFRequestData(requestId, productId, bpnNumber);
		if (pcfRequestData.isPresent()) {

			PcfRequestEntity entity = pcfRequestData.get();
			entity.setStatus(PCFRequestStatusEnum.APPROVED);
			savePcfRequest(entity);

			// push api call
			pushPCFDataToConsumer(calculatedPCFValue, productId, bpnNumber, requestId, message);

			entity.setStatus(PCFRequestStatusEnum.PUSHED);
			savePcfRequest(entity);
		}
	}

	@Override
	public PcfRequestModel savePcfRequestData(String requestId, String productId, String bpnNumber, String message,
			PCFTypeEnum type) {

		PcfRequestModel pcfRequest = PcfRequestModel.builder().requestId(requestId).productId(productId)
				.bpnNumber(bpnNumber).status(PCFRequestStatusEnum.REQUESTED).message(message).type(type)
				.requestedTime(LocalDateTime.now()).lastUpdatedTime(LocalDateTime.now()).build();

		PcfRequestEntity pcfRequestEntity = pcfMapper.mapFrom(pcfRequest);
		return pcfMapper.mapFrom(savePcfRequest(pcfRequestEntity));
	}

	@Override
	public void recievedPCFData(String productId, String bpnNumber, String requestId, String message, String pcfData) {

		String responseId = UUID.randomUUID().toString();

		PcfResponseEntity entity = PcfResponseEntity.builder().pcfData(pcfData).requestId(requestId)
				.responseId(responseId).lastUpdatedTime(LocalDateTime.now()).build();

		pcfReqsponseRepository.save(entity);

		Optional<PcfRequestEntity> pcfRequestData = getPCFRequestData(requestId, productId, bpnNumber);

		if (pcfRequestData.isPresent()) {
			PcfRequestEntity entity1 = pcfRequestData.get();
			entity1.setStatus(PCFRequestStatusEnum.RECEIVED);
			savePcfRequest(entity1);
		}
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
	private void pushPCFDataToConsumer(JsonObject calculatedPCFValue, String productId, String bpnNumber,
			String requestId, String message) {

		// 1 fetch EDC connectors and DTR Assets from EDC connectors
		List<QueryDataOfferModel> pcfExchangeUrlOffers = edcAssetUrlCacheService.getPCFExchangeUrlFromTwin(bpnNumber);

		// 2 lookup shell for PCF sub model
		for (QueryDataOfferModel dtOffer : pcfExchangeUrlOffers) {

			EDRCachedByIdResponse edrToken = edcAssetUrlCacheService.verifyAndGetToken(bpnNumber, dtOffer);

			if (edrToken != null) {
				URI pcfpushEnpoint = new URI(edrToken.getEndpoint());
				Map<String, String> header = new HashMap<>();
				header.put(edrToken.getAuthKey(), edrToken.getAuthCode());
				pcfExchangeProxy.uploadPcfSubmodel(pcfpushEnpoint, header, productId, bpnNumber, requestId, message,
						calculatedPCFValue);
			} else {
				log.warn("EDC connector " + dtOffer.getConnectorOfferUrl()
						+ ", The EDR token is null to find pcf exchange asset");
			}

		}

	}

	@SneakyThrows
	private PcfRequestEntity savePcfRequest(PcfRequestEntity pcfRequestEntity) {
		pcfRequestEntity.setLastUpdatedTime(LocalDateTime.now());
		log.info("'" + pcfRequestEntity.getProductId() + "' pcf request saved in the database successfully");
		pcfRequestRepository.save(pcfRequestEntity);
		return pcfRequestEntity;
	}

	private Optional<PcfRequestEntity> getPCFRequestData(String requestedId, String productId, String bpnNumber) {
		return pcfRequestRepository.findByRequestIdAndProductIdAndBpnNumber(requestedId, productId, bpnNumber);

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
			ResponseEntity<ShellLookupResponse> shellLookup = eDCDigitalTwinProxyForLookUp
					.shellLookup(new URI(endpoint), shellLookupRequest.toJsonString(), header);
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
					.getShellDescriptorByShellId(new URI(endpoint), encodeShellIdBase64Utf8(shellIds.get(0)), header);
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

	private String encodeShellIdBase64Utf8(String shellId) {
		return Base64.getUrlEncoder().encodeToString(shellId.getBytes());
	}
}
