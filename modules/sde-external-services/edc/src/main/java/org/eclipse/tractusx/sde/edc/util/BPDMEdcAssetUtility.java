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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.utils.LogUtil;
import org.eclipse.tractusx.sde.edc.constants.EDCAssetConfigurableConstant;
import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.Criterion;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.portal.model.ConnectorInfo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BPDMEdcAssetUtility {

	private final EDCAssetLookUp edcAssetLookUp;
	
	private final EDCAssetConfigurableConstant edcAssetConfigurableConstant;
	
	@Cacheable(value = "bpn-bpdm", key = "#bpnNumber")
	public List<QueryDataOfferModel> getBpdmUrl(String bpnNumber) {
		
		ConnectorInfo confo =ConnectorInfo.builder().bpn(bpnNumber)
				.connectorEndpoint(List.of(edcAssetConfigurableConstant.getBpdmProviderEdcDataspaceApi()))
				.build();
		
		return edcAssetLookUp.getEDCAssetsByType(List.of(confo), getFilterCriteria());
	}

	@CacheEvict(value = "bpn-bpdm", key = "#bpnNumber")
	public void removeBpdmCache(String bpnNumber) {
		log.info(LogUtil.encode("Cleared '" + bpnNumber + "' bpn-bpdm cache"));
	}

	@CacheEvict(value = "bpn-bpdm", allEntries = true)
	public void cleareBpdmAllCache() {
		log.info("Cleared All bpn-pcfexchange cache");
	}
	
	private List<Criterion> getFilterCriteria() {

		List<Criterion> criterias = new ArrayList<>();

		List<String> edcBPDMSearchCriteriaList = edcAssetConfigurableConstant.getEdcBPDMSearchCriteria();
		for (String edcBPDMSearchCriteria : edcBPDMSearchCriteriaList) {
			if (StringUtils.isNotBlank(edcBPDMSearchCriteria)) {
					String[] split1 = edcBPDMSearchCriteria.split("@");
					if (split1.length == 2) {
						criterias.add(Criterion.builder()
								.operandLeft(split1[0])
								.operator("=")
								.operandRight(split1[1])
								.build());
					}
			}
		}
		return criterias;
	}

}
