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
package org.eclipse.tractusx.sde.core.service;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tractusx.sde.edc.util.UtilityFunctions;
import org.eclipse.tractusx.sde.portal.api.IPartnerPoolExternalServiceApi;
import org.eclipse.tractusx.sde.portal.api.IPortalExternalServiceApi;
import org.eclipse.tractusx.sde.portal.model.ConnectorInfo;
import org.eclipse.tractusx.sde.portal.model.LegalEntityData;
import org.eclipse.tractusx.sde.portal.model.response.LegalEntityResponse;
import org.eclipse.tractusx.sde.portal.model.response.UnifiedBPNValidationStatusEnum;
import org.eclipse.tractusx.sde.portal.model.response.UnifiedBpnValidationResponse;
import org.eclipse.tractusx.sde.portal.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class PortalProxyService {

	@Value("${portal.backend.hostname}")
	private String portalBackendHostname;
	
	private final CacheUtilityService cacheUtilityService;

	private final IPortalExternalServiceApi portalExternalServiceApi;
 
	private final IPartnerPoolExternalServiceApi partnerPoolExternalServiceApi;

	private final KeycloakUtil keycloakUtil;

	@SneakyThrows
	public List<LegalEntityResponse> fetchLegalEntitiesData(String searchText, Integer page, Integer size) {
		List<LegalEntityResponse> result = new ArrayList<>();
		LegalEntityData legalEntity = partnerPoolExternalServiceApi.fetchLegalEntityData(searchText, page, size,
				UtilityFunctions.getAuthToken());
		if (null != legalEntity) {
			legalEntity.getContent().stream().forEach(companyData -> {
				companyData.getLegalEntity().getNames().stream().forEach(name -> {
					LegalEntityResponse legalEntityResponse = LegalEntityResponse.builder()
							.bpn(companyData.getLegalEntity().getBpn()).name(name.getValue()).build();
					result.add(legalEntityResponse);
				});
			});
		}
		return result;
	}

	@SneakyThrows
	public List<ConnectorInfo> fetchConnectorInfo(List<String> bpns) {
		String token = keycloakUtil.getKeycloakToken();
		return portalExternalServiceApi.fetchConnectorInfo(bpns, "Bearer " + token);
	}

	@SneakyThrows
	public UnifiedBpnValidationResponse unifiedBpnValidation(String bpn) {

		List<ConnectorInfo> connectorsInfo = fetchConnectorInfo(List.of(bpn));

		UnifiedBpnValidationResponse unifiedBpnValidationResponse = UnifiedBpnValidationResponse.builder()
				.message(bpn + " for BPN number found valid Connector in partner network")
				.status(UnifiedBPNValidationStatusEnum.FULL_PARTNER).build();

		if (connectorsInfo.isEmpty()) {
			
			List<String> memberBPNDataList = cacheUtilityService.getPartner() ;
			if (!memberBPNDataList.isEmpty() && memberBPNDataList.contains(bpn)) {
				unifiedBpnValidationResponse.setMessage(
						bpn + " BPN number is part of partner network but there is no valid Connector found");
				unifiedBpnValidationResponse.setStatus(UnifiedBPNValidationStatusEnum.PARTNER);
			} else {
				unifiedBpnValidationResponse.setMessage(bpn + " BPN number is not part of partner network");
				unifiedBpnValidationResponse.setStatus(UnifiedBPNValidationStatusEnum.NOT_PARTNER);
			}
		}
		return unifiedBpnValidationResponse;
	}
}
