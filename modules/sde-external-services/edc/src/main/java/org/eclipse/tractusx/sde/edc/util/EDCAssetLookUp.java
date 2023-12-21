package org.eclipse.tractusx.sde.edc.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.edc.services.ConsumerControlPanelService;
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

	private final ConsumerControlPanelService consumerControlPanelService;

	@Value("${edc.consumer.hostname}")
	private String consumerHost;

	private String filterExpressionTemplate = """
			 "filterExpression": [{
			    "operandLeft": "https://w3id.org/edc/v0.0.1/ns/type",
			    "operator": "=",
			    "operandRight": "%s"
			}]""";

	public List<QueryDataOfferModel> getEDCAssetsByType(String bpnNumber, String assetType) {

		List<ConnectorInfo> connectorInfos = portalProxyService.fetchConnectorInfo(List.of(bpnNumber));

		List<QueryDataOfferModel> offers = new ArrayList<>();

		String filterExpression = String.format(filterExpressionTemplate, assetType);

		connectorInfos.stream().forEach(
				connectorInfo -> connectorInfo.getConnectorEndpoint().parallelStream().distinct().forEach(connector -> {
					try {
						if (!connector.contains(consumerHost)) {
							List<QueryDataOfferModel> queryDataOfferModel = consumerControlPanelService
									.queryOnDataOffers(connector, 0, 100, filterExpression);

							log.info("For Connector " + connector + ", found " + assetType + " assets :"
									+ queryDataOfferModel.size());

							queryDataOfferModel.forEach(each -> each.setConnectorOfferUrl(connector));

							offers.addAll(queryDataOfferModel);
						} else {
							log.info("The Consumer and Provider Connector are same so ignoring it for lookup "
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
