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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.edc.services.CatalogResponseBuilder;
import org.eclipse.tractusx.sde.portal.handler.PortalProxyService;
import org.eclipse.tractusx.sde.portal.model.ConnectorInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EDCAssetLookUp {

	private final PortalProxyService portalProxyService;
	private final CatalogResponseBuilder catalogResponseBuilder;

	@Value("${edc.consumer.hostname}")
	private String consumerHost;

	private String filterExpressionTemplate = """
			 "filterExpression": [{
                "operandLeft": "'http://purl.org/dc/terms/type'.'@id'",
                "operator": "=",
                "operandRight": "https://w3id.org/catenax/taxonomy#%s"
			}]""";

	public List<QueryDataOfferModel> getEDCAssetsByType(String bpnNumber, String assetType) {

		List<ConnectorInfo> connectorInfos = portalProxyService.fetchConnectorInfo(List.of(bpnNumber));
		
		List<ConnectorInfo> distinctList = connectorInfos.stream().distinct().toList();

		List<QueryDataOfferModel> offers = new ArrayList<>();

		String filterExpression = String.format(filterExpressionTemplate, assetType);

		distinctList.stream().forEach(
				connectorInfo -> connectorInfo.getConnectorEndpoint().parallelStream().distinct().forEach(connector -> {
					try {
						if (!connector.contains(consumerHost)) {
							
							List<QueryDataOfferModel> queryDataOfferModel = catalogResponseBuilder
									.queryOnDataOffers(connector, bpnNumber, 0, 100, filterExpression);

							log.info("For Connector " + connector + ", found " + assetType + " assets :"
									+ queryDataOfferModel.size());

							queryDataOfferModel.forEach(each -> each.setConnectorOfferUrl(connector));

							offers.addAll(queryDataOfferModel);
						
						} else {
							log.warn("The Consumer and Provider Connector are same so ignoring it for lookup "
									+ assetType + " in to " + connector);
						}

					} catch (Exception e) {
						log.error("Error while looking EDC catalog for " + assetType + ", " + connector
								+ ", Exception :" + e.getMessage());
					}
				}));

		return offers;
	}

}