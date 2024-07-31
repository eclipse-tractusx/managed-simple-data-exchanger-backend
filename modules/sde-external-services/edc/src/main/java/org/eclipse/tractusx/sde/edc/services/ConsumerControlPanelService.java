/********************************************************************************
 * Copyright (c) 2022,2024 T-Systems International GmbH
 * Copyright (c) 2022,2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.edc.services;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.tractusx.sde.bpndiscovery.handler.BpnDiscoveryProxyService;
import org.eclipse.tractusx.sde.bpndiscovery.model.request.BpnDiscoverySearchRequest;
import org.eclipse.tractusx.sde.bpndiscovery.model.request.BpnDiscoverySearchRequest.Search;
import org.eclipse.tractusx.sde.bpndiscovery.model.response.BpnDiscoveryResponse;
import org.eclipse.tractusx.sde.bpndiscovery.model.response.BpnDiscoverySearchResponse;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.edc.entities.database.ContractNegotiationInfoEntity;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ActionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyConstraintBuilderService;
import org.eclipse.tractusx.sde.edc.facilitator.ContractNegotiateManagementHelper;
import org.eclipse.tractusx.sde.edc.facilitator.EDRRequestHelper;
import org.eclipse.tractusx.sde.edc.gateways.database.ContractNegotiationInfoRepository;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractNegotiationDto;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedByIdResponse;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedResponse;
import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.edc.model.request.Offer;
import org.eclipse.tractusx.sde.edc.model.request.QueryDataOfferRequest;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.edc.util.EDCAssetUrlCacheService;
import org.eclipse.tractusx.sde.edc.util.UtilityFunctions;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerControlPanelService {

	private static final String STATUS = "status";

	private final ContractNegotiateManagementHelper contractNegotiateManagement;

	private final ContractNegotiationInfoRepository contractNegotiationInfoRepository;
	private final PolicyConstraintBuilderService policyConstraintBuilderService;

	private final EDRRequestHelper edrRequestHelper;
	private final BpnDiscoveryProxyService bpnDiscoveryProxyService;
	private final EDCAssetUrlCacheService edcAssetUrlCacheService;
	private final ContractNegotiationService contractNegotiationService;
	private final LookUpDTTwin lookUpDTTwin;

	public Set<QueryDataOfferModel> queryOnDataOffers(String manufacturerPartId, String searchBpnNumber,
			String submodel, Integer offset, Integer limit) {

		List<QueryDataOfferModel> queryOnDataOffers = new LinkedList<>();
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

					queryOnDataOffers.addAll(lookUpDTTwin.lookUpTwin(edrToken, dtOffer, manufacturerPartId, bpnNumber,
							submodel, offset, limit));

				} else {
					log.warn("EDR token is null, unable to look Up Digital Twin for :" + dtOffer.toString());
				}
			}
		}
		return new HashSet<>(queryOnDataOffers);

	}

	@Async
	public void subscribeDataOffers(ConsumerRequest consumerRequest, String processId) {

		HashMap<String, String> extensibleProperty = new HashMap<>();
		AtomicReference<String> negotiateContractId = new AtomicReference<>();
		AtomicReference<ContractNegotiationDto> checkContractNegotiationStatus = new AtomicReference<>();

		List<ActionRequest> action = policyConstraintBuilderService
				.getUsagePoliciesConstraints(consumerRequest.getUsagePolicies());

		consumerRequest.getOffers().parallelStream().forEach(offer -> {
			try {
				negotiateContractId.set(contractNegotiateManagement.negotiateContract(offer.getConnectorOfferUrl(),
						offer.getConnectorId(), offer.getOfferId(), offer.getAssetId(), action, extensibleProperty));
				int retry = 3;
				int counter = 1;

				do {
					Thread.sleep(3000);
					checkContractNegotiationStatus
							.set(contractNegotiateManagement.checkContractNegotiationStatus(negotiateContractId.get()));
					counter++;
				} while (checkContractNegotiationStatus.get() != null
						&& !checkContractNegotiationStatus.get().getState().equals("FINALIZED")
						&& !checkContractNegotiationStatus.get().getState().equals("TERMINATED") && counter <= retry);

			} catch (InterruptedException ie) {
				log.error("Exception in subscribeDataOffers" + ie.getMessage());
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				log.error("Exception in subscribeDataOffers" + e.getMessage());
			} finally {
				ContractNegotiationInfoEntity contractNegotiationInfoEntity = ContractNegotiationInfoEntity.builder()
						.id(UUID.randomUUID().toString()).processId(processId).connectorId(offer.getConnectorId())
						.offerId(offer.getOfferId())
						.contractNegotiationId(negotiateContractId != null ? negotiateContractId.get() : null)
						.status(checkContractNegotiationStatus.get() != null
								? checkContractNegotiationStatus.get().getState()
								: "Failed:Exception")
						.dateTime(LocalDateTime.now()).build();

				contractNegotiationInfoRepository.save(contractNegotiationInfoEntity);
			}
		});

	}

	public Map<String, Object> subcribeAndDownloadOffer(Offer offer, List<ActionRequest> action,
			boolean flagToDownloadImidiate, String downloadAs) {

		Map<String, Object> resultFields = new ConcurrentHashMap<>();
		try {
			var recipientURL = UtilityFunctions.removeLastSlashOfUrl(offer.getConnectorOfferUrl());
			EDRCachedResponse checkContractNegotiationStatus = contractNegotiationService
					.verifyOrCreateContractNegotiation(offer.getConnectorId(), Map.of(), recipientURL, action, offer);

			resultFields.put("edr", checkContractNegotiationStatus);

			doVerifyResult(offer.getAssetId(), checkContractNegotiationStatus);

			if (flagToDownloadImidiate)
				resultFields.put("data", downloadFile(checkContractNegotiationStatus, downloadAs));

			resultFields.put(STATUS, "SUCCESS");

		} catch (FeignException e) {
			log.error("Feign RequestBody: " + e.request());
			String errorMsg = "Unable to complete subscribeAndDownloadDataOffers because: " + e.contentUTF8();
			log.error(errorMsg);
			prepareErrorMap(resultFields, errorMsg);
		} catch (Exception e) {
			log.error("SubscribeAndDownloadDataOffers Oops! We have -" + e.getMessage());
			String errorMsg = "Unable to complete subscribeAndDownloadDataOffers because: " + e.getMessage();
			prepareErrorMap(resultFields, errorMsg);
		}

		return resultFields;
	}

	@SneakyThrows
	private void doVerifyResult(String assetId, EDRCachedResponse checkContractNegotiationStatus)
			throws ServiceException {

		if (checkContractNegotiationStatus != null
				&& StringUtils.isBlank(checkContractNegotiationStatus.getTransferProcessId())
				&& StringUtils.isNoneBlank(checkContractNegotiationStatus.getAgreementId())) {
			throw new ServiceException("There is valid contract agreement exist for " + assetId
					+ " but intiate data transfer is not completed and no EDR token available, download is not possible");
		}

		if (Optional.ofNullable(checkContractNegotiationStatus).isEmpty())
			throw new ServiceException("Time out!! to get 'EDC EDR status to download data");
	}

	@SneakyThrows
	public Map<String, Object> downloadFileFromEDCUsingifAlreadyTransferStatusCompleted(List<String> assetIdList,
			String type) {
		Map<String, Object> response = new ConcurrentHashMap<>();
		assetIdList.parallelStream().forEach(assetId -> {

			Map<String, Object> downloadResultFields = new ConcurrentHashMap<>();
			try {
				EDRCachedResponse verifyEDRRequestStatus = contractNegotiationService.verifyEDRRequestStatus(assetId);

				downloadResultFields.put("edr", verifyEDRRequestStatus);

				doVerifyResult(assetId, verifyEDRRequestStatus);

				downloadResultFields.put("data", downloadFile(verifyEDRRequestStatus, type));

				downloadResultFields.put(STATUS, "SUCCESS");
			} catch (Exception e) {
				String errorMsg = e.getMessage();
				log.error("We have exception: " + errorMsg);
				prepareErrorMap(downloadResultFields, errorMsg);
			} finally {
				response.put(assetId, downloadResultFields);
			}
		});
		return response;
	}

	private void prepareErrorMap(Map<String, Object> resultFields, String errorMsg) {
		resultFields.put(STATUS, "FAILED");
		resultFields.put("error", errorMsg);
	}

	@SneakyThrows
	private Object downloadFile(EDRCachedResponse verifyEDRRequestStatus, String downloadDataAs) {
		if (verifyEDRRequestStatus != null) {
			try {
				EDRCachedByIdResponse authorizationToken = contractNegotiationService
						.getAuthorizationTokenForDataDownload(verifyEDRRequestStatus.getTransferProcessId());
				String endpoint = authorizationToken.getEndpoint() + "?type=" + downloadDataAs;
				return edrRequestHelper.getDataFromProvider(authorizationToken, endpoint);
			} catch (FeignException e) {
				log.error("FeignException Download RequestBody: " + e.request());
				String errorMsg = "Unable to download subcribe data offer because: " + e.contentUTF8();
				throw new ServiceException(errorMsg);
			} catch (Exception e) {
				log.error("Exception DownloadFileFromEDCUsingifAlreadyTransferStatusCompleted Oops! We have -"
						+ e.getMessage());
				String errorMsg = "Unable to download subcribe data offer because: " + e.getMessage();
				throw new ServiceException(errorMsg);
			}
		}
		return null;
	}

	public List<QueryDataOfferModel> getEDCPolicy(List<QueryDataOfferRequest> queryDataOfferModel) {

		Map<Pair<String, String>, List<QueryDataOfferRequest>> collect = queryDataOfferModel.stream()
				.collect(Collectors.groupingBy(ele -> Pair.of(ele.getConnectorOfferUrl(), ele.getPublisher())));

		return collect.entrySet().stream().map(entry -> lookUpDTTwin.getEDCOffer(entry.getValue(), entry.getKey()))
				.flatMap(Collection::stream).toList();
	}

}