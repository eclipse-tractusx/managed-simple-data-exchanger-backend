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
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.entities.SubmodelJsonRequest;
import org.eclipse.tractusx.sde.common.model.PagingResponse;
import org.eclipse.tractusx.sde.common.validators.ValidatePolicyTemplate;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellLookupRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellLookupResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelResponse;
import org.eclipse.tractusx.sde.digitaltwins.gateways.external.EDCDigitalTwinProxyForLookUp;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedByIdResponse;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.pcfexchange.entity.PcfRequestEntity;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFOptionsEnum;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFRequestStatusEnum;
import org.eclipse.tractusx.sde.pcfexchange.mapper.PcfExchangeMapper;
import org.eclipse.tractusx.sde.pcfexchange.repository.PcfRequestRepository;
import org.eclipse.tractusx.sde.pcfexchange.request.PcfRequestModel;
import org.eclipse.tractusx.sde.pcfexchange.response.PcfExchangeResponse;
import org.eclipse.tractusx.sde.pcfexchange.service.IPCFExchangeService;
import org.eclipse.tractusx.sde.pcfexchange.utils.DDTRUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import feign.FeignException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class PcfExchangeServiceImpl implements IPCFExchangeService {

	private final PcfRequestRepository pcfRequestRepository;
	private final PcfExchangeMapper pcfMapper;
	private final DDTRUtils dDTRUtils;
	private final EDCDigitalTwinProxyForLookUp eDCDigitalTwinProxyForLookUp;

	@Override
	public PcfExchangeResponse findPcfDataOffer(String productId, String bpnNumber) {

		PcfRequestModel pcfRequest = PcfRequestModel.builder()
				.productId(productId)
				.bpnNumber(bpnNumber)
				.build();

		// 1 fetch EDC connectors and DTR Assets from EDC connectors
		List<QueryDataOfferModel> dDTRDataOffers = dDTRUtils.getDDTRUrl(bpnNumber);
		
		ShellLookupRequest shellLookupRequest = getShellLookupRequest(pcfRequest);

		ShellDescriptorResponse shellDescriptorResponse = null;
		String msg = "";

		PcfExchangeResponse pcfExchangeResponse = PcfExchangeResponse.builder()
				.shellDescriptorResponse(shellDescriptorResponse)
				.build();
		
		// 2 lookup shell for PCF sub model
		for (QueryDataOfferModel dtOffer : dDTRDataOffers) {

			EDRCachedByIdResponse edrToken = dDTRUtils.verifyAndGetToken(bpnNumber, dtOffer);

			if (edrToken != null) {
				shellDescriptorResponse = lookUpPCFTwin(shellLookupRequest, pcfRequest, edrToken, dtOffer);
				if (shellDescriptorResponse != null) {
					pcfExchangeResponse.setShellDescriptorResponse(shellDescriptorResponse);
					pcfExchangeResponse.setStatus(PCFOptionsEnum.SUBSCRIBE_DATA_OFFER);
					break;
				} else {
					log.warn("EDC connector " + dtOffer.getConnectorOfferUrl() + ", No PCF twin found for "
							+ shellLookupRequest.toJsonString());
				}
			} else {
				msg = " EDC connector " + dtOffer.getConnectorOfferUrl() + ", The EDR token is null to find PCF twin ";
				log.warn(msg + shellLookupRequest.toJsonString());
			}
		}

		// 3 If PCF data not found request for PCF data to provider
		if (dDTRDataOffers.isEmpty() || shellDescriptorResponse == null) {
			log.info("No PCF aspect shell found for  for " + shellLookupRequest.toJsonString()
					+ ", Need to request for PCF data");
			pcfExchangeResponse.setStatus(PCFOptionsEnum.REQUESTE_DATA_OFFER);
		}
		
		return pcfExchangeResponse;
	}

	@Override
	public void approveAndPushPCFData(String productId, String bpnNumber, String requestId, String message,
			@Valid @ValidatePolicyTemplate SubmodelJsonRequest pcfSubmodelJsonRequest) {
		
		PcfRequestModel approvePCFRequest = PcfRequestModel.builder()
				.requestId(requestId)
				.productId(productId)
				.bpnNumber(bpnNumber)
				.message(message)
				.build();

		updatePcfRequest(approvePCFRequest);
		
		//push PCF data
		
		// find out consumer pcf exchange asset put api call for update pushed
		
		
	}
	
	
	@Override
	public PcfRequestModel savePcfRequestData(String requestId, String productId, String bpnNumber, String message) {

		PcfRequestModel pcfRequest = PcfRequestModel.builder()
				.requestId(requestId)
				.productId(productId)
				.bpnNumber(bpnNumber)
				.message(message)
				.build();

		return savePcfRequest(pcfRequest);
	}
	
	@SneakyThrows
	private PcfRequestModel savePcfRequest(PcfRequestModel request) {
		
		PcfRequestEntity pcfRequestEntity = null; 
		
		if (isPcfAlreadyRequestedforProduct(request.getRequestId(), request.getProductId())) {
			pcfRequestEntity = pcfMapper.mapFrom(request);
			pcfRequestEntity.setStatus(PCFRequestStatusEnum.REQUESTED);
			pcfRequestEntity.setRequestedTime(LocalDateTime.now());
			pcfRequestEntity.setLastUpdatedTime(LocalDateTime.now());
		} else {
			pcfRequestEntity = getPCFRequestData(request);
			pcfRequestEntity.setStatus(PCFRequestStatusEnum.REREQUESTED);
			pcfRequestEntity.setLastUpdatedTime(LocalDateTime.now());
		}
		
		log.info("'" + request.getProductId() + "' pcf request saved in the database successfully");
		pcfRequestRepository.save(pcfRequestEntity);
		
		// Email notification process here to notify caller of pcf request
		return request;
	}
	
	private PcfRequestModel updatePcfRequest(PcfRequestModel request) {

		PcfRequestEntity pcfRequestEntity = null; 
		if (!isPcfAlreadyRequestedforProduct(request.getRequestId(), request.getProductId())) {
			pcfRequestEntity = getPCFRequestData(request);
			pcfRequestEntity.setStatus(PCFRequestStatusEnum.PUSHED);
			pcfRequestEntity.setMessage("Requested PCF Data Pueshed and ready to SUBSCRIBE and DOWNLOAD");
			pcfRequestEntity.setLastUpdatedTime(LocalDateTime.now());
		}
		
		log.info("'" + request.getProductId() + "' pcf request status updated as Pushed the database successfully");
		pcfRequestRepository.save(pcfRequestEntity);

		return request;
	}

	private PcfRequestEntity getPCFRequestData(PcfRequestModel request) {
		
		 Optional<PcfRequestEntity> optionalPcfRequestEntity= pcfRequestRepository.findByRequestIdAndProductId(request.getRequestId(), request.getProductId());

		 if(!optionalPcfRequestEntity.isEmpty()) {
			 return optionalPcfRequestEntity.get();
		 }
		 return null;
	}


	private boolean isPcfAlreadyRequestedforProduct(String requestId, String productId) {

		return pcfRequestRepository.findByRequestIdAndProductId(requestId, productId).isEmpty();
	}
	
	@Override
	public PagingResponse getAllPcfRequestData(String type, Integer page, Integer pageSize) {
		
		
		Page<PcfRequestEntity> result = pcfRequestRepository
				.findAll(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "lastUpdatedTime")));
		List<PcfRequestModel> requestList = result.stream().map(pcfMapper::mapFrom).toList();
		
		return PagingResponse.builder()
				.items(requestList)
				.pageSize(result.getSize())
				.page(result.getNumber())
				.totalItems(result.getTotalElements())
				.build();
	}
	
	private ShellLookupRequest getShellLookupRequest(PcfRequestModel pcfRequest) {

		ShellLookupRequest shellLookupRequest = new ShellLookupRequest();
		getSpecificAssetIds(pcfRequest).entrySet().stream()
				.forEach(entry -> shellLookupRequest.addLocalIdentifier(entry.getKey(), entry.getValue()));

		return shellLookupRequest;
	}

	private Map<String, String> getSpecificAssetIds(PcfRequestModel pcfRequest) {
		Map<String, String> specificIdentifiers = new HashMap<>();
		specificIdentifiers.put(CommonConstants.MANUFACTURER_PART_ID, pcfRequest.getProductId());
		specificIdentifiers.put(CommonConstants.MANUFACTURER_ID, pcfRequest.getBpnNumber());
		specificIdentifiers.put(CommonConstants.ASSET_LIFECYCLE_PHASE, CommonConstants.AS_PLANNED);

		return specificIdentifiers;
	}

	@SneakyThrows
	private ShellDescriptorResponse lookUpPCFTwin(ShellLookupRequest shellLookupRequest, PcfRequestModel pcfRequest,
			EDRCachedByIdResponse edrToken, QueryDataOfferModel dtOffer) {

		ShellDescriptorResponse shellDescriptorResponseBody = new ShellDescriptorResponse();
		String endpoint = edrToken.getEndpoint();
		String dtOfferUrl = dtOffer.getConnectorOfferUrl();
		try {

			Map<String, String> header = Map.of(edrToken.getAuthKey(), edrToken.getAuthCode());

			ResponseEntity<ShellLookupResponse> shellLookup = eDCDigitalTwinProxyForLookUp
					.shellLookup(new URI(endpoint), shellLookupRequest.toJsonString(), header);
			ShellLookupResponse body = shellLookup.getBody();

			if (shellLookup.getStatusCode() == HttpStatus.OK && body != null) {
				shellDescriptorResponseBody = getPCFSubmodelDetails(shellLookupRequest, endpoint, header, dtOfferUrl,
						body.getResult());
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

		return shellDescriptorResponseBody;
	}

	@SneakyThrows
	private ShellDescriptorResponse getPCFSubmodelDetails(ShellLookupRequest shellLookupRequest, String endpoint,
			Map<String, String> header, String dtOfferUrl, List<String> shellIds) {

		ShellDescriptorResponse shellDescriptorResponseWithPcfSubmodel = new ShellDescriptorResponse();
		if (shellIds == null) {
			log.warn(dtOfferUrl + ", No shell found for PCF aspect : " + shellLookupRequest.toJsonString());
		} else if (shellIds.size() > 1) {
			log.warn(String.format("Multiple shell id's found for look up PCF Aspect %s, %s", dtOfferUrl,
					shellLookupRequest.toJsonString()));
		} else if (shellIds.size() == 1) {
			ResponseEntity<ShellDescriptorResponse> shellDescriptorResponse = eDCDigitalTwinProxyForLookUp
					.getShellDescriptorByShellId(new URI(endpoint), encodeShellIdBase64Utf8(shellIds.get(0)), header);
			ShellDescriptorResponse shellDescriptorResponseBody = shellDescriptorResponse.getBody();
			if (shellDescriptorResponse.getStatusCode() == HttpStatus.OK && shellDescriptorResponseBody != null) {

				for (SubModelResponse subModelResponse : shellDescriptorResponseBody.getSubmodelDescriptors()) {
					if (!subModelResponse.getIdShort().isEmpty() && subModelResponse.getIdShort().contains("pcf")) {
						shellDescriptorResponseWithPcfSubmodel = shellDescriptorResponseBody;

					}

				}
			}
		}

		return shellDescriptorResponseWithPcfSubmodel;
	}

	private String encodeShellIdBase64Utf8(String shellId) {
		return Base64.getUrlEncoder().encodeToString(shellId.getBytes());
	}
}
