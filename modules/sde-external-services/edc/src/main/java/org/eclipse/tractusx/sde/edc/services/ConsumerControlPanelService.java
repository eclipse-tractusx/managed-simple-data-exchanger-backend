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

package org.eclipse.tractusx.sde.edc.services;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.tractusx.sde.common.entities.Policies;
import org.eclipse.tractusx.sde.common.enums.PolicyAccessEnum;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.edc.api.ContractOfferCatalogApi;
import org.eclipse.tractusx.sde.edc.constants.EDCAssetConstant;
import org.eclipse.tractusx.sde.edc.entities.database.ContractNegotiationInfoEntity;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ActionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyConstraintBuilderService;
import org.eclipse.tractusx.sde.edc.facilitator.AbstractEDCStepsHelper;
import org.eclipse.tractusx.sde.edc.facilitator.ContractNegotiateManagementHelper;
import org.eclipse.tractusx.sde.edc.facilitator.EDRRequestHelper;
import org.eclipse.tractusx.sde.edc.gateways.database.ContractNegotiationInfoRepository;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractNegotiationDto;
import org.eclipse.tractusx.sde.edc.model.contractoffers.ContractOfferRequestFactory;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedByIdResponse;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedResponse;
import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.edc.model.request.OfferRequest;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.edc.util.UtilityFunctions;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerControlPanelService extends AbstractEDCStepsHelper {

	private static final String NEGOTIATED = "NEGOTIATED";

	private final ContractOfferCatalogApi contractOfferCatalogApiProxy;
	private final ContractNegotiateManagementHelper contractNegotiateManagement;

	private final ContractNegotiationInfoRepository contractNegotiationInfoRepository;
	private final PolicyConstraintBuilderService policyConstraintBuilderService;

	private final ContractOfferRequestFactory contractOfferRequestFactory;

	private final EDRRequestHelper edrRequestHelper;

	private static final Integer RETRY = 5;

	private static final Integer THRED_SLEEP_TIME = 5000;

	public List<QueryDataOfferModel> queryOnDataOffers(String providerUrl, Integer offset, Integer limit,
			String filterExpression) {

		String sproviderUrl = UtilityFunctions.removeLastSlashOfUrl(providerUrl);

		List<QueryDataOfferModel> queryOfferResponse = new ArrayList<>();

		JsonNode contractOfferCatalog = contractOfferCatalogApiProxy
				.getContractOffersCatalog(contractOfferRequestFactory
						.getContractOfferRequest(sproviderUrl + protocolPath, limit, offset, filterExpression));

		JsonNode jOffer = contractOfferCatalog.get("dcat:dataset");
		if (jOffer.isArray()) {

			jOffer.forEach(
					offer -> queryOfferResponse.add(buildContractOffer(sproviderUrl, contractOfferCatalog, offer)));

		} else {
			queryOfferResponse.add(buildContractOffer(sproviderUrl, contractOfferCatalog, jOffer));
		}

		return queryOfferResponse;
	}

	private QueryDataOfferModel buildContractOffer(String sproviderUrl, JsonNode contractOfferCatalog, JsonNode offer) {

		JsonNode policy = offer.get("odrl:hasPolicy");

		String edcstr = "edc:";

		QueryDataOfferModel build = QueryDataOfferModel.builder()
				.assetId(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_ID))
				.connectorOfferUrl(sproviderUrl + File.separator + getFieldFromJsonNode(offer, "@id"))
				.offerId(getFieldFromJsonNode(policy, "@id"))
				.title(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_NAME))
				.type(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_TYPE))
				.description(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_DESCRIPTION))
				.created(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_CREATED))
				.modified(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_MODIFIED))
				.publisher(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_PUBLISHER))
				.version(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_VERSION))
				.fileName(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_FILENAME))
				.fileContentType(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_CONTENTTYPE))
				.connectorId(getFieldFromJsonNode(contractOfferCatalog, "edc:participantId")).build();

		checkAndSetPolicyPermission(build, policy);

		return build;
	}

	private void checkAndSetPolicyPermission(QueryDataOfferModel build, JsonNode policy) {

		if (policy != null && policy.isArray()) {
			policy.forEach(pol -> {
				JsonNode permission = pol.get("odrl:permission");
				checkAndSetPolicyPermissionConstraints(build, permission);
			});
		} else if (policy != null) {
			JsonNode permission = policy.get("odrl:permission");
			checkAndSetPolicyPermissionConstraints(build, permission);
		}
	}

	private void checkAndSetPolicyPermissionConstraints(QueryDataOfferModel build, JsonNode permission) {

		JsonNode constraints = permission.get("odrl:constraint");

		List<Policies> usagePolicies = new ArrayList<>();

		List<String> bpnNumbers = new ArrayList<>();

		if (constraints != null) {
			JsonNode jsonNode = constraints.get("odrl:and");

			if (jsonNode != null && jsonNode.isArray()) {
				jsonNode.forEach(constraint -> setConstraint(usagePolicies, bpnNumbers, constraint));
			} else if (jsonNode != null) {
				setConstraint(usagePolicies, bpnNumbers, jsonNode);
			}
		}
		build.setTypeOfAccess(!bpnNumbers.isEmpty() ? PolicyAccessEnum.RESTRICTED : PolicyAccessEnum.UNRESTRICTED);
		build.setBpnNumbers(bpnNumbers);
		build.setUsagePolicies(usagePolicies);
	}

	private void setConstraint(List<Policies> usagePolicies, List<String> bpnNumbers, JsonNode jsonNode) {

		String leftOperand = getFieldFromJsonNode(jsonNode, "odrl:leftOperand");
		String rightOperand = getFieldFromJsonNode(jsonNode, "odrl:rightOperand");

		if (leftOperand.equals("BusinessPartnerNumber")) {
			bpnNumbers.add(rightOperand);
		} else {
			Policies policyResponse = UtilityFunctions.identyAndGetUsagePolicy(leftOperand, rightOperand);
			if (policyResponse != null)
				usagePolicies.add(policyResponse);
		}
	}

	private String getFieldFromJsonNode(JsonNode jnode, String fieldName) {
		if (jnode.get(fieldName) != null)
			return jnode.get(fieldName).asText();
		else
			return "";
	}

	@Async
	public void subscribeDataOffers(ConsumerRequest consumerRequest, String processId) {

		HashMap<String, String> extensibleProperty = new HashMap<>();
		AtomicReference<String> negotiateContractId = new AtomicReference<>();
		AtomicReference<ContractNegotiationDto> checkContractNegotiationStatus = new AtomicReference<>();

		var recipientURL = UtilityFunctions.removeLastSlashOfUrl(consumerRequest.getProviderUrl());

		List<Policies> policies = consumerRequest.getPolicies();

		

		
		ActionRequest action = policyConstraintBuilderService.getUsagePoliciesConstraints(policies);
		consumerRequest.getOffers().parallelStream().forEach(offer -> {
			try {

				negotiateContractId.set(
						contractNegotiateManagement.negotiateContract(recipientURL, consumerRequest.getConnectorId(),
								offer.getOfferId(), offer.getAssetId(), action, extensibleProperty));
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

				// Local DB entry
				ContractNegotiationInfoEntity contractNegotiationInfoEntity = ContractNegotiationInfoEntity.builder()
						.id(UUID.randomUUID().toString()).processId(processId)
						.connectorId(consumerRequest.getConnectorId()).offerId(offer.getOfferId())
						.contractNegotiationId(negotiateContractId != null ? negotiateContractId.get() : null)
						.status(checkContractNegotiationStatus.get() != null
								? checkContractNegotiationStatus.get().getState()
								: "Failed:Exception")
						.dateTime(LocalDateTime.now()).build();

				contractNegotiationInfoRepository.save(contractNegotiationInfoEntity);
			}
		});

	}

	@SneakyThrows
	public EDRCachedResponse verifyOrCreateContractNegotiation(String connectorId,
			Map<String, String> extensibleProperty, String recipientURL, ActionRequest action, OfferRequest offer) {
		// Verify if there already EDR process initiated then skip t for again download
		List<EDRCachedResponse> eDRCachedResponseList = edrRequestHelper.getEDRCachedByAsset(offer.getAssetId());
		EDRCachedResponse checkContractNegotiationStatus = verifyEDRResponse(eDRCachedResponseList);

		if (checkContractNegotiationStatus == null) {
			log.info("There was no EDR process initiated or may be EDR token was expired " + offer.getAssetId()
					+ ", so initiating EDR process");
			edrRequestHelper.edrRequestInitiate(recipientURL, connectorId, offer.getOfferId(), offer.getAssetId(),
					action, extensibleProperty);
		} else {
			log.info("There was EDR process initiated " + offer.getAssetId() + ", so ignoring EDR process initiation");
		}

		if (checkContractNegotiationStatus == null || !NEGOTIATED.equals(checkContractNegotiationStatus.getEdrState()))
			checkContractNegotiationStatus = verifyEDRRequestStatus(offer.getAssetId());

		return checkContractNegotiationStatus;
	}

	@SneakyThrows
	public EDRCachedResponse verifyEDRRequestStatus(String assetId) {
		EDRCachedResponse eDRCachedResponse = null;
		String edrStatus = "NewToSDE";
		List<EDRCachedResponse> eDRCachedResponseList = null;
		int counter = 1;
		try {
			do {
				if (counter > 1)
					Thread.sleep(THRED_SLEEP_TIME);
				eDRCachedResponseList = edrRequestHelper.getEDRCachedByAsset(assetId);
				eDRCachedResponse = verifyEDRResponse(eDRCachedResponseList);

				if (eDRCachedResponse != null)
					edrStatus = eDRCachedResponse.getEdrState();

				log.info("Verifying 'NEGOTIATED' EDC EDR status to download data for '" + assetId
						+ "', The current status is '" + edrStatus + "', Attempt " + counter);
				counter++;
			} while (counter <= RETRY && !NEGOTIATED.equals(edrStatus));

			if (eDRCachedResponse == null)
				throw new ServiceException("Time out!! unable to get EDR negotiated status");

		} catch (FeignException e) {
			log.error("RequestBody: " + e.request());
			String errorMsg = "FeignExceptionton for asset " + assetId + "," + e.contentUTF8();
			log.error("Response: " + errorMsg);
			throw new ServiceException(errorMsg);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			String errorMsg = "InterruptedException for asset " + assetId + "," + ie.getMessage();
			log.error(errorMsg);
			throw new ServiceException(errorMsg);
		} catch (Exception e) {
			String errorMsg = "Exception for asset " + assetId + "," + e.getMessage();
			log.error(errorMsg);
			throw new ServiceException(errorMsg);
		}
		return eDRCachedResponse;
	}

	private EDRCachedResponse verifyEDRResponse(List<EDRCachedResponse> eDRCachedResponseList) {
		EDRCachedResponse eDRCachedResponse = null;
		if (eDRCachedResponseList != null && !eDRCachedResponseList.isEmpty()) {
			for (EDRCachedResponse edrCachedResponseObj : eDRCachedResponseList) {
				String edrState = edrCachedResponseObj.getEdrState();
				// For EDC connector 5.0 edrState filed not supported so checking token
				// validation by calling direct API
				if (NEGOTIATED.equalsIgnoreCase(edrState) || isEDRTokenValid(edrCachedResponseObj)) {
					eDRCachedResponse = edrCachedResponseObj;
					eDRCachedResponse.setEdrState(NEGOTIATED);
					break;
				}
			}
		}
		return eDRCachedResponse;
	}

	@SneakyThrows
	private boolean isEDRTokenValid(EDRCachedResponse edrCachedResponseObj) {
		String assetId = edrCachedResponseObj.getAssetId();
		try {
			edrRequestHelper.getDataFromProvider(
					getAuthorizationTokenForDataDownload(edrCachedResponseObj.getTransferProcessId()));
		} catch (FeignException e) {
			log.error("RequestBody: " + e.request());
			String errorMsg = "FeignExceptionton for verifyEDR token " + assetId + "," + e.status() + "::"
					+ e.contentUTF8();
			log.error("Response: " + errorMsg);

			if (e.status() == 403) {
				log.error("Got 403 as token status so going to try new EDR token: " + errorMsg);
				return false;
			}
		} catch (Exception e) {
			String errorMsg = "Exception for asset in isEDRTokenValid " + assetId + "," + e.getMessage();
			log.error(errorMsg);
			throw new ServiceException(errorMsg);
		}
		return true;
	}

	@SneakyThrows
	public EDRCachedByIdResponse getAuthorizationTokenForDataDownload(String transferProcessId) {
		return edrRequestHelper.getEDRCachedByTransferProcessId(transferProcessId);
	}

}
