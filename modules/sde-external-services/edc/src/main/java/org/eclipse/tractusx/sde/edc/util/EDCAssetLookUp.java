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

import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.Criterion;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.edc.services.CatalogResponseBuilder;
import org.eclipse.tractusx.sde.portal.handler.PortalProxyService;
import org.eclipse.tractusx.sde.portal.model.ConnectorInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

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
			"filterExpression": %s
			""";

	public List<QueryDataOfferModel> getEDCAssetsByType(String bpnNumber, List<Criterion> filtercriteria) {

		List<ConnectorInfo> connectorInfos = portalProxyService.fetchConnectorInfo(List.of(bpnNumber));
		List<ConnectorInfo> distinctList = connectorInfos.stream().distinct().toList();
		return getEDCAssetsByType(distinctList, filtercriteria);

	}

	public List<QueryDataOfferModel> getEDCAssetsByType(List<ConnectorInfo> distinctList,
			List<Criterion> filterCriteria) {

		List<QueryDataOfferModel> offers = new ArrayList<>();
		
		Gson gson = new Gson();
		String filterCriteriaList = gson.toJson(filterCriteria);
		String filterExpression = String.format(filterExpressionTemplate, filterCriteriaList);

		distinctList.stream().forEach(
				connectorInfo -> connectorInfo.getConnectorEndpoint().parallelStream().distinct().forEach(connector -> {
					try {
						if (!connector.contains(consumerHost)) {

							List<QueryDataOfferModel> queryDataOfferModel = catalogResponseBuilder
									.queryOnDataOffers(connector, connectorInfo.getBpn(), 0, 100, filterExpression);

							log.info("For Connector " + connector + ", found " + filterCriteria.toString() + " assets :"
									+ queryDataOfferModel.size());

							queryDataOfferModel.forEach(each -> each.setConnectorOfferUrl(connector));

							offers.addAll(queryDataOfferModel);

						} else {
							log.warn("The Consumer and Provider Connector are same so ignoring it for lookup "
									+ filterCriteria.toString() + " in to " + connector);
						}

					} catch (Exception e) {
						log.error("Error while looking EDC catalog for " + filterCriteria.toString() + ", " + connector
								+ ", Exception :" + e.getMessage());
					}
				}));

		return offers;
	}
}