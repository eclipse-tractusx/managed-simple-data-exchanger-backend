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
package org.eclipse.tractusx.sde.edc.util;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.edc.constants.EDCAssetConfigurableConstant;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ActionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyConstraintBuilderService;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedByIdResponse;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedResponse;
import org.eclipse.tractusx.sde.edc.model.request.Offer;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.edc.services.ContractNegotiationService;
import org.springframework.stereotype.Service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class EDCAssetUrlCacheService {

	private static final Map<String, LocalDateTime> dDTRmap = new ConcurrentHashMap<>();
	private static final Map<String, LocalDateTime> pcfExchangeURLMap = new ConcurrentHashMap<>();
	private static final Map<String, LocalDateTime> bpdmMap = new ConcurrentHashMap<>();

	private final ContractNegotiationService contractNegotiationService;
	private final PolicyConstraintBuilderService policyConstraintBuilderService;

	private final DDTRUrlCacheUtility dDTRUrlCacheUtility;
	private final PCFExchangeAssetUtils pcfExchangeAssetUtils;
	private final BPDMEdcAssetUtility bpdmEdcAssetUtility;
	
	private final EDCAssetConfigurableConstant edcAssetConfigurableConstant;

	@SneakyThrows
	public EDRCachedByIdResponse verifyAndGetToken(String bpnNumber, QueryDataOfferModel queryDataOfferModel) {

		List<ActionRequest> action = policyConstraintBuilderService
				.getUsagePoliciesConstraints(queryDataOfferModel.getPolicy().getUsagePolicies());

		Offer offer = Offer.builder().assetId(queryDataOfferModel.getAssetId())
				.offerId(queryDataOfferModel.getOfferId()).policyId(queryDataOfferModel.getPolicyId())
				.connectorId(queryDataOfferModel.getConnectorId())
				.connectorOfferUrl(queryDataOfferModel.getConnectorOfferUrl())
				.hasPolicy(queryDataOfferModel.getHasPolicy())
				.build();
		try {
			EDRCachedResponse eDRCachedResponse = contractNegotiationService.verifyOrCreateContractNegotiation(
					bpnNumber, Map.of(), queryDataOfferModel.getConnectorOfferUrl(), action, offer);

			if (eDRCachedResponse == null || StringUtils.isBlank(eDRCachedResponse.getTransferProcessId())) {
				throw new ServiceException("Time out!! to get EDC EDR status to lookup '"
						+ queryDataOfferModel.getConnectorOfferUrl() + ", " + queryDataOfferModel.getAssetId()
						+ "', The current status is null");
			} else
				return contractNegotiationService
						.getAuthorizationTokenForDataDownload(eDRCachedResponse.getTransferProcessId());

		} catch (FeignException e) {
			log.error("FeignException Request : " + e.request());
			String errorMsg = "Unable to look up offer because: " + e.contentUTF8();
			log.error("FeignException : " + errorMsg);
		} catch (Exception e) {
			String errorMsg = "Unable to look up offer because: " + e.getMessage();
			log.error("Exception : " + errorMsg);
		}

		return null;
	}

	// dDTR
	public List<QueryDataOfferModel> getDDTRUrl(String bpnNumber) {

		LocalDateTime cacheExpTime = dDTRmap.get(bpnNumber);
		LocalDateTime currDate = LocalDateTime.now();

		if (cacheExpTime == null)
			cacheExpTime = currDate.plusHours(12);
		else if (currDate.isAfter(cacheExpTime)) {
			dDTRUrlCacheUtility.removeDDTRUrlCache(bpnNumber);
			cacheExpTime = currDate.plusHours(12);
		}
		dDTRmap.put(bpnNumber, cacheExpTime);
		List<QueryDataOfferModel> ddtrUrl = dDTRUrlCacheUtility.getDDTRUrl(bpnNumber);
		if (ddtrUrl.isEmpty()) {
			log.info("Found connector list empty so removing existing cache and retry to fetch");
			removeDDTRUrlCache(bpnNumber);
		}
		return ddtrUrl;
	}

	public void clearDDTRUrlCache() {
		dDTRmap.clear();
		dDTRUrlCacheUtility.cleareDDTRUrlAllCache();
	}

	public void removeDDTRUrlCache(String bpnNumber) {
		dDTRUrlCacheUtility.removeDDTRUrlCache(bpnNumber);
		dDTRmap.remove(bpnNumber);
	}

	// PCF
	public List<QueryDataOfferModel> getPCFExchangeUrlFromTwin(String bpnNumber) {

		LocalDateTime cacheExpTime = pcfExchangeURLMap.get(bpnNumber);
		LocalDateTime currDate = LocalDateTime.now();

		if (cacheExpTime == null)
			cacheExpTime = currDate.plusHours(12);
		else if (currDate.isAfter(cacheExpTime)) {
			pcfExchangeAssetUtils.removePCFExchangeCache(bpnNumber);
			cacheExpTime = currDate.plusHours(12);
		}
		pcfExchangeURLMap.put(bpnNumber, cacheExpTime);
		List<QueryDataOfferModel> pcfExchangeurls = pcfExchangeAssetUtils.getPCFExchangeUrl(bpnNumber);
		if (pcfExchangeurls.isEmpty()) {
			log.info("Found connector list empty so removing existing cache and retry to fetch");
			removePCFExchangeCache(bpnNumber);
		}
		return pcfExchangeurls;
	}

	public void clearPCFExchangeUrlCache() {
		pcfExchangeURLMap.clear();
		pcfExchangeAssetUtils.clearePCFExchangeAllCache();
	}

	public void removePCFExchangeCache(String bpnNumber) {
		pcfExchangeAssetUtils.removePCFExchangeCache(bpnNumber);
		pcfExchangeURLMap.remove(bpnNumber);
	}
	
	//BPDM
	public List<QueryDataOfferModel> getBpdmUrl() {

		LocalDateTime cacheExpTime = bpdmMap.get(edcAssetConfigurableConstant.getBpdmProviderBpnl());
		LocalDateTime currDate = LocalDateTime.now();

		if (cacheExpTime == null)
			cacheExpTime = currDate.plusHours(12);
		else if (currDate.isAfter(cacheExpTime)) {
			bpdmEdcAssetUtility.removeBpdmCache(edcAssetConfigurableConstant.getBpdmProviderBpnl());
			cacheExpTime = currDate.plusHours(12);
		}
		bpdmMap.put(edcAssetConfigurableConstant.getBpdmProviderBpnl(), cacheExpTime);
		List<QueryDataOfferModel> bpdmUrls = bpdmEdcAssetUtility.getBpdmUrl(edcAssetConfigurableConstant.getBpdmProviderBpnl());
		if (bpdmUrls.isEmpty()) {
			log.info("Found connector list empty so removing existing cache and retry to fetch");
			removeBpdmCache();
		}
		return bpdmUrls;
	}

	public void clearBpdmUrlCache() {
		bpdmMap.clear();
		bpdmEdcAssetUtility.cleareBpdmAllCache();
	}

	public void removeBpdmCache() {
		bpdmEdcAssetUtility.removeBpdmCache(edcAssetConfigurableConstant.getBpdmProviderBpnl());
		bpdmMap.remove(edcAssetConfigurableConstant.getBpdmProviderBpnl());
	}
}