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

package org.eclipse.tractusx.sde.edc.services;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.utils.LogUtil;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ActionRequest;
import org.eclipse.tractusx.sde.edc.enums.Type;
import org.eclipse.tractusx.sde.edc.facilitator.AbstractEDCStepsHelper;
import org.eclipse.tractusx.sde.edc.facilitator.ContractNegotiateManagementHelper;
import org.eclipse.tractusx.sde.edc.facilitator.EDRRequestHelper;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractNegotiationDto;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedByIdResponse;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedResponse;
import org.eclipse.tractusx.sde.edc.model.request.Offer;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractNegotiationService  extends AbstractEDCStepsHelper {
	
	private static final String NEGOTIATED = "NEGOTIATED";
	private final EDRRequestHelper edrRequestHelper;
	private static final Integer RETRY = 5;
	private static final Integer THRED_SLEEP_TIME = 5000;
	
	private final ContractNegotiateManagementHelper contractNegotiateManagement;
	
	@SneakyThrows
	public EDRCachedResponse verifyOrCreateContractNegotiation(String connectorId,
			Map<String, String> extensibleProperty, String recipientURL, ActionRequest action, Offer offer) {

		if (!offer.getConnectorOfferUrl().endsWith(protocolPath))
			recipientURL = recipientURL + protocolPath;

		// Verify if there already EDR process initiated then skip it for again download
		String assetId = offer.getAssetId();
		List<EDRCachedResponse> eDRCachedResponseList = edrRequestHelper.getEDRCachedByAsset(assetId);
		EDRCachedResponse checkContractNegotiationStatus = verifyEDRResponse(eDRCachedResponseList);

		if (checkContractNegotiationStatus == null) {
			String contractAgreementId = checkandGetContractAgreementId(assetId);
			if (StringUtils.isBlank(contractAgreementId)) {
				log.info(LogUtil.encode("The EDR process was not completed, no 'NEGOTIATED' EDR status found "
						+ "and not valid contract agreementId for " + assetId + ", so initiating EDR process"));
				edrRequestHelper.edrRequestInitiate(recipientURL, connectorId, offer.getOfferId(), assetId, action,
						extensibleProperty);
				checkContractNegotiationStatus = verifyEDRRequestStatus(assetId);
			} else {
				log.info(LogUtil.encode("There is valid contract agreement exist for " + assetId
						+ ", so ignoring EDR process initiation"));
				checkContractNegotiationStatus = EDRCachedResponse.builder().agreementId(contractAgreementId)
						.assetId(assetId).build();
			}
		} else {
			log.info(LogUtil.encode("There was EDR process initiated " + assetId
					+ ", so ignoring EDR process initiation, going to check EDR status only"));
			if (!NEGOTIATED.equals(checkContractNegotiationStatus.getEdrState()))
				checkContractNegotiationStatus = verifyEDRRequestStatus(assetId);
		}

		return checkContractNegotiationStatus;
	}
	
	@SneakyThrows
	private String checkandGetContractAgreementId(String assetId) {
		
		List<JsonNode> contractAgreements = contractNegotiateManagement.getAllContractAgreements(assetId,
				Type.CONSUMER.name(), 0, 10);
		String contractAgreementId = null;
		if (!contractAgreements.isEmpty())
			for (JsonNode jsonNode : contractAgreements) {
				ContractNegotiationDto checkContractAgreementNegotiationStatus = contractNegotiateManagement
						.checkContractAgreementNegotiationStatus(getFieldFromJsonNode(jsonNode, "@id"));
				if ("FINALIZED".equals(checkContractAgreementNegotiationStatus.getState())) {
					contractAgreementId = checkContractAgreementNegotiationStatus.getContractAgreementId();
					break;
				}
			}

		return contractAgreementId;
	}
	
	private EDRCachedResponse verifyEDRResponse(List<EDRCachedResponse> eDRCachedResponseList) {
		EDRCachedResponse eDRCachedResponse = null;
		if (eDRCachedResponseList != null && !eDRCachedResponseList.isEmpty()) {
			for (EDRCachedResponse edrCachedResponseObj : eDRCachedResponseList) {
				String edrState = edrCachedResponseObj.getEdrState();
				// For EDC connector 5.0 edrState field not supported so checking token
				// validation by calling direct API
				if (NEGOTIATED.equalsIgnoreCase(edrState) || isEDRTokenValid(edrCachedResponseObj)) {
					eDRCachedResponse = edrCachedResponseObj;
					eDRCachedResponse.setEdrState(NEGOTIATED);
					break;
				}
				eDRCachedResponse = edrCachedResponseObj;
			}
		}
		return eDRCachedResponse;
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

				if (eDRCachedResponse != null && eDRCachedResponse.getEdrState() != null)
					edrStatus = eDRCachedResponse.getEdrState();

				log.info(LogUtil.encode("Verifying 'NEGOTIATED' EDC EDR status to download data for '" + assetId
						+ "', The current status is '" + edrStatus + "', Attempt " + counter));
				counter++;
			} while (counter <= RETRY && !NEGOTIATED.equals(edrStatus));

			if (eDRCachedResponse == null) {
				String contractAgreementId = checkandGetContractAgreementId(assetId);
				if (StringUtils.isNoneBlank(contractAgreementId)) {
					eDRCachedResponse = EDRCachedResponse.builder().agreementId(contractAgreementId).assetId(assetId)
							.build();
				} else
					throw new ServiceException("Time out!! unable to get Contract negotiation FINALIZED status");
			}

		} catch (FeignException e) {
			log.error("RequestBody: " + e.request());
			String errorMsg = "FeignExceptionton for asset " + assetId + "," + e.contentUTF8();
			log.error(LogUtil.encode("Response: " + errorMsg));
			throw new ServiceException(errorMsg);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			String errorMsg = "InterruptedException for asset " + assetId + "," + ie.getMessage();
			log.error(LogUtil.encode(errorMsg));
			throw new ServiceException(errorMsg);
		} catch (Exception e) {
			String errorMsg = "Exception for asset " + assetId + "," + e.getMessage();
			log.error(LogUtil.encode(errorMsg));
			throw new ServiceException(errorMsg);
		}
		return eDRCachedResponse;
	}

	@SneakyThrows
	private boolean isEDRTokenValid(EDRCachedResponse edrCachedResponseObj) {
		String assetId = edrCachedResponseObj.getAssetId();
		try {
			EDRCachedByIdResponse authorizationToken = getAuthorizationTokenForDataDownload(
					edrCachedResponseObj.getTransferProcessId());
			edrRequestHelper.getDataFromProvider(authorizationToken, authorizationToken.getEndpoint());
		} catch (FeignException e) {
			log.error("FeignException RequestBody: " + e.request());
			String errorMsg = "FeignExceptionton for verifyEDR token " + assetId + "," + e.status() + "::"
					+ e.contentUTF8();
			log.error("FeignException Response: " + errorMsg);

			if (e.status() == 403) {
				log.error("Got 403 as token status so going to try new EDR token: " + errorMsg);
				return false;
			}
		} catch (Exception e) {
			String errorMsg = "Exception for asset in isEDRTokenValid " + assetId + "," + e.getMessage();
			log.error(errorMsg);
		}
		return true;
	}

	
	@SneakyThrows
	public EDRCachedByIdResponse getAuthorizationTokenForDataDownload(String transferProcessId) {
		return edrRequestHelper.getEDRCachedByTransferProcessId(transferProcessId);
	}

	private String getFieldFromJsonNode(JsonNode jnode, String fieldName) {
		if (jnode.get(fieldName) != null)
			return jnode.get(fieldName).asText();
		else
			return "";
	}

}