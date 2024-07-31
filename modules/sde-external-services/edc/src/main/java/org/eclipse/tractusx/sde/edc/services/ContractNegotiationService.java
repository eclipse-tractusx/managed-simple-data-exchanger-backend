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
public class ContractNegotiationService extends AbstractEDCStepsHelper {

	private final EDRRequestHelper edrRequestHelper;
	private static final Integer RETRY = 5;
	private static final Integer THRED_SLEEP_TIME = 5000;

	private final ContractNegotiateManagementHelper contractNegotiateManagement;

	@SneakyThrows
	public EDRCachedResponse verifyOrCreateContractNegotiation(String connectorId,
			Map<String, String> extensibleProperty, String recipientURL, List<ActionRequest> action, Offer offer) {

		if (!offer.getConnectorOfferUrl().endsWith(protocolPath))
			recipientURL = recipientURL + protocolPath;

		// Verify if there already EDR process initiated then skip it for again download
		String assetId = offer.getAssetId();
		List<EDRCachedResponse> eDRCachedResponseList = edrRequestHelper.getEDRCachedByAsset(assetId);
		EDRCachedResponse checkContractNegotiationStatus = verifyEDRResponse(eDRCachedResponseList);

		if (checkContractNegotiationStatus == null) {
			String contractAgreementId = checkandGetContractAgreementId(assetId);
			if (StringUtils.isBlank(contractAgreementId) || !eDRCachedResponseList.isEmpty()) {
				log.info(LogUtil.encode("The EDR process was not completed, no EDR status found "
						+ "and not valid contract agreementId for " + recipientURL + ", " + assetId
						+ ", so initiating EDR process"));
				edrRequestHelper.edrRequestInitiate(recipientURL, connectorId, offer, assetId, action,
						extensibleProperty);
				checkContractNegotiationStatus = verifyEDRRequestStatus(assetId);
			} else {
				log.info(LogUtil.encode("There is valid contract agreement exist for " + recipientURL + ", " + assetId
						+ ", so ignoring EDR process initiation"));
				checkContractNegotiationStatus = EDRCachedResponse.builder().agreementId(contractAgreementId)
						.assetId(assetId).build();
			}
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
				try {
					getAuthorizationTokenForDataDownload(edrCachedResponseObj.getTransferProcessId());
					eDRCachedResponse = edrCachedResponseObj;
					break;
				} catch (Exception e) {
					log.error(LogUtil.encode("The EDR token has expired for " + edrCachedResponseObj.getTransferProcessId()));
				}
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

				if (eDRCachedResponse != null)
					edrStatus = "FoundEDR";

				log.info(LogUtil.encode("Verifying EDC EDR status to download data for '" + assetId + "', The current status is '"
						+ edrStatus + "', Attempt " + counter));
				counter++;
			} while (counter <= RETRY && eDRCachedResponse == null);

			if (eDRCachedResponse == null) {
				String contractAgreementId = checkandGetContractAgreementId(assetId);
				if (StringUtils.isNoneBlank(contractAgreementId)) {
					eDRCachedResponse = EDRCachedResponse.builder().agreementId(contractAgreementId).assetId(assetId)
							.build();
				} else
					throw new ServiceException(
							"Time out!! unable to get Contract negotiation FINALIZED status for " + assetId);
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