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
package org.eclipse.tractusx.sde.pcfexchange.service.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.mapper.JsonObjectMapper;
import org.eclipse.tractusx.sde.common.utils.LogUtil;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedByIdResponse;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.edc.util.EDCAssetUrlCacheService;
import org.eclipse.tractusx.sde.pcfexchange.enums.PCFRequestStatusEnum;
import org.eclipse.tractusx.sde.pcfexchange.proxy.PCFExchangeProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProxyRequestInterface {

	private static final String PRODUCT_IDS = "productIds";
	private static final String SLASH_DELIMETER = "/";
	private final PCFExchangeProxy pcfExchangeProxy;
	private final PCFRepositoryService pcfRepositoryService;
	private final EDCAssetUrlCacheService edcAssetUrlCacheService;
	private final JsonObjectMapper jsonObjectMapper;
	
	@Value(value = "${manufacturerId}")
	private String manufacturerId;

	@Value(value = "${digital-twins.managed.thirdparty:false}")
	private boolean managedThirdParty;
	
	@SneakyThrows
	public void requestToProviderForPCFValue(String productId, StringBuilder sb, String requestId, String message,
			QueryDataOfferModel dataset, boolean isRequestToNonexistingTwin) {

		EDRCachedByIdResponse edrToken = edcAssetUrlCacheService.verifyAndGetToken(dataset.getConnectorId(), dataset);

		if (!sb.isEmpty())
			sb.append("\n");

		if (edrToken != null) {
			
			URI pcfpushEnpoint = null;
			
			if(isRequestToNonexistingTwin)
				pcfpushEnpoint = new URI(
						edrToken.getEndpoint() + SLASH_DELIMETER + PRODUCT_IDS + SLASH_DELIMETER + productId);
			else
				pcfpushEnpoint = new URI(edrToken.getEndpoint());

			Map<String, String> header = new HashMap<>();
			header.put("authorization", edrToken.getAuthorization());

			// Send request to data provider for PCF value push
			pcfExchangeProxy.getPcfByProduct(pcfpushEnpoint, header, manufacturerId,
					requestId, message);

			sb.append(productId + ": requested for PCF value");
			pcfRepositoryService.savePcfStatus(requestId, PCFRequestStatusEnum.REQUESTED);
		} else {
			sb.append(productId + ": Unable to request for PCF value becasue the EDR token status is null");
			log.warn(LogUtil.encode("EDC connector " + dataset.getConnectorOfferUrl() + ":"+ requestId +","+ productId +
					"Unable to request for PCF value becasue the EDR token status is null"));
			pcfRepositoryService.savePcfStatus(requestId, PCFRequestStatusEnum.FAILED);
		}
	}
	
	@SneakyThrows
	public void sendNotificationToConsumer(PCFRequestStatusEnum status, JsonObject calculatedPCFValue,
			String productId, String bpnNumber, String requestId, String message) {

		// 1 fetch EDC connectors and DTR Assets from EDC connectors
		List<QueryDataOfferModel> pcfExchangeUrlOffers = edcAssetUrlCacheService.getPCFExchangeUrlFromTwin(bpnNumber);

		// 2 lookup shell for PCF sub model and send notification to call consumer
		// request
		if(pcfExchangeUrlOffers.isEmpty()) {
			pcfRepositoryService.updatePCFPushStatus(status, requestId, "Unable to find PCF exchange endpoint");
		}
		else {
			pcfExchangeUrlOffers.parallelStream().forEach(dtOffer -> {
				
				if (PCFRequestStatusEnum.SENDING_REJECT_NOTIFICATION.equals(status)) {
					sendNotification(null, productId, bpnNumber, requestId, dtOffer, status, message);
				} else {
					sendNotification(calculatedPCFValue, productId, bpnNumber, requestId, dtOffer, status, message);
				}
				
			});
		}
	}

	@SneakyThrows
	private void sendNotification(JsonObject calculatedPCFValue, String productId, String bpnNumber, String requestId,
			QueryDataOfferModel dtOffer, PCFRequestStatusEnum status, String message) {
		String sendNotificationStatus = "";
		try {
			EDRCachedByIdResponse edrToken = edcAssetUrlCacheService.verifyAndGetToken(bpnNumber, dtOffer);

			if (edrToken != null) {
				
				URI pcfpushEnpoint = new URI(
						edrToken.getEndpoint() + SLASH_DELIMETER + PRODUCT_IDS + SLASH_DELIMETER + productId);

				Map<String, String> header = new HashMap<>();
				header.put("authorization", edrToken.getAuthorization());

				pcfExchangeProxy.uploadPcfSubmodel(pcfpushEnpoint, header, bpnNumber, requestId, message,
						jsonObjectMapper.gsonObjectToJsonNode(calculatedPCFValue));

				sendNotificationStatus = "SUCCESS";
			} else {
				String warn="EDC connector " + dtOffer.getConnectorOfferUrl()
				+ ", The EDR token is null to find pcf exchange asset";
				log.warn(warn);
				sendNotificationStatus = warn;
			}
		} catch (FeignException e) {
			log.error("FeignRequest:" + e.request());
			String errorMsg = "Unable to send notification to consumer because: "
					+ (StringUtils.isBlank(e.contentUTF8()) ? e.getMessage() : e.contentUTF8());
			log.error("FeignException : " + errorMsg);
			sendNotificationStatus = errorMsg;
		} finally {
			pcfRepositoryService.updatePCFPushStatus(status, requestId, sendNotificationStatus);
		}
	}



}