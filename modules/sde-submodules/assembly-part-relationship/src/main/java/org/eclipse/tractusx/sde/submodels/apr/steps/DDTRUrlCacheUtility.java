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

package org.eclipse.tractusx.sde.submodels.apr.steps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedByIdResponse;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedResponse;
import org.eclipse.tractusx.sde.edc.model.request.OfferRequest;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.edc.services.ConsumerControlPanelService;
import org.eclipse.tractusx.sde.portal.handler.PortalProxyService;
import org.eclipse.tractusx.sde.portal.model.ConnectorInfo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DDTRUrlCacheUtility {

	private final PortalProxyService portalProxyService;

	private final ConsumerControlPanelService consumerControlPanelService;

	@Cacheable(value = "bpn-ddtr", key = "#bpnNumber")
	public List<QueryDataOfferModel> getDDTRUrl(String bpnNumber) {

		List<ConnectorInfo> connectorInfos = portalProxyService.fetchConnectorInfo(List.of(bpnNumber));

		List<QueryDataOfferModel> offers = new ArrayList<>();

		String filterExpression = String.format("""
				 "filterExpression": [{
				    "operandLeft": "https://w3id.org/edc/v0.0.1/ns/type",
				    "operator": "=",
				    "operandRight": "data.core.digitalTwinRegistry"
				}]""");

		connectorInfos.stream().forEach(
				connectorInfo -> connectorInfo.getConnectorEndpoint().parallelStream().distinct().forEach(connector -> {
					try {
						List<QueryDataOfferModel> queryDataOfferModel = consumerControlPanelService
								.queryOnDataOffers(connector, 0, 100, filterExpression);
						log.info("For Connector " + connector + ", found asset :" + queryDataOfferModel.size());

						queryDataOfferModel.forEach(each -> each.setConnectorOfferUrl(connector));

						offers.addAll(queryDataOfferModel);
					} catch (Exception e) {
						log.error("Error while looking EDC catalog for digitaltwin registry url, " + connector
								+ ", Exception :" + e.getMessage());
					}
				}));

		return offers;
	}

	@SneakyThrows
	public EDRCachedByIdResponse verifyAndGetToken(String bpnNumber, QueryDataOfferModel queryDataOfferModel) {
		OfferRequest offer = OfferRequest.builder().assetId(queryDataOfferModel.getAssetId())
				.offerId(queryDataOfferModel.getOfferId()).policyId(queryDataOfferModel.getPolicyId()).build();
		try {
			EDRCachedResponse eDRCachedResponse = consumerControlPanelService.verifyOrCreateContractNegotiation(
					bpnNumber, Map.of(), queryDataOfferModel.getConnectorOfferUrl(), null, offer);

			if (eDRCachedResponse == null) {
				throw new ServiceException(
						"Time out!! to get 'NEGOTIATED' EDC EDR status to lookup dt twin, The current status is null");
			} else if (!"NEGOTIATED".equalsIgnoreCase(eDRCachedResponse.getEdrState())) {
				throw new ServiceException(
						"Time out!! to get 'NEGOTIATED' EDC EDR status to lookup dt twin, The current status is '"
								+ eDRCachedResponse.getEdrState() + "'");
			} else
				return consumerControlPanelService
						.getAuthorizationTokenForDataDownload(eDRCachedResponse.getTransferProcessId());

		} catch (FeignException e) {
			String errorMsg = "Unable to look up offer because: " + e.contentUTF8();
			log.error("FeignException : " + errorMsg);
		} catch (Exception e) {
			String errorMsg = "Unable to look up offer because: " + e.getMessage();
			log.error("Exception : " + errorMsg);
		}

		return null;
	}

	@CacheEvict(value = "bpn-ddtr", key = "#bpnNumber")
	public void removeDDTRUrlCache(String bpnNumber) {
		log.info("Cleared '" + bpnNumber + "' bpn-ddtr cache");
	}

	@CacheEvict(value = "bpn-ddtr", allEntries = true)
	public void cleareDDTRUrlAllCache() {
		log.info("Cleared All bpn-ddtr cache");
	}

}