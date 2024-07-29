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

package org.eclipse.tractusx.sde.core.service;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.utils.LogUtil;
import org.eclipse.tractusx.sde.edc.constants.EDCAssetConfigurableConstant;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedByIdResponse;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.edc.util.EDCAssetUrlCacheService;
import org.eclipse.tractusx.sde.portal.api.IPartnerPoolExternalServiceApi;
import org.eclipse.tractusx.sde.portal.model.LegalEntityData;
import org.eclipse.tractusx.sde.portal.model.response.LegalEntityResponse;
import org.springframework.stereotype.Service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartnerPoolService {
	
	private final EDCAssetUrlCacheService edcAssetUrlCacheService;
	
	private final EDCAssetConfigurableConstant edcAssetConfigurableConstant;
	
	private final IPartnerPoolExternalServiceApi partnerPoolExternalServiceApi;
	
	@SneakyThrows
	public List<LegalEntityResponse> fetchLegalEntitiesData(String bpnLs, String searchText, Integer page, Integer size) {
		
		List<LegalEntityResponse> legalEntityResponseList = new LinkedList<>();
		List<QueryDataOfferModel> ddTROffers = edcAssetUrlCacheService.getBpdmUrl();
		
		for (QueryDataOfferModel dtOffer : ddTROffers) {

			EDRCachedByIdResponse edrToken = edcAssetUrlCacheService.verifyAndGetToken(edcAssetConfigurableConstant.getBpdmProviderBpnl(), dtOffer);
			
			if (edrToken != null) {
				
				LegalEntityData legalEntity = fetchLegalEntityDataByEdrToken(bpnLs, searchText, edrToken, page, size );
				legalEntityResponseList.addAll(legalEntity.getContent().stream().map(companyData ->
				LegalEntityResponse.builder()
				.bpn(companyData.getBpnl())
				.name(companyData.getLegalName())
				.build())
				.toList());
				
			} else {
				log.warn("EDR token is null, unable to fetch legal entities data for :" + dtOffer.toString());
				return Collections.emptyList();
			}
		}
		
		return Optional.ofNullable(legalEntityResponseList).orElse(Collections.emptyList());  
	}

	
	private LegalEntityData fetchLegalEntityDataByEdrToken(String bpnLs, String legalName, EDRCachedByIdResponse edrToken, Integer page, Integer size) {

		LegalEntityData legalEntityData = null;
		try {

			Map<String, String> header = new HashMap<>();
			header.put("authorization", edrToken.getAuthorization());
			URI endpoint = new URI(edrToken.getEndpoint());
			
			legalEntityData = partnerPoolExternalServiceApi.fetchLegalEntityData(endpoint, bpnLs, legalName, page, size, header);

		} catch (FeignException e) {
			String err = e.contentUTF8();
			err = StringUtils.isBlank(err) ? e.getMessage() : err;
			String errorMsg = "Unable to fetch LegalEntity Data for  " + legalName + "Or BpnLs " + bpnLs + " because: "
					+ err;
			log.error(LogUtil.encode("FeignException : " + errorMsg));
		} catch (Exception e) {
			String errorMsg = "Unable to fetch LegalEntity Data for  " + legalName + "Or BpnLs " + bpnLs + "because: "
					+ e.getMessage();
			log.error(LogUtil.encode("Exception : " + errorMsg));
		}
		return legalEntityData;
	}
}
