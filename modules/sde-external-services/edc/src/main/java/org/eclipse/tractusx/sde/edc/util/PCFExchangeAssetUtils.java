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

import java.util.List;

import org.eclipse.tractusx.sde.common.configuration.properties.PCFAssetStaticPropertyHolder;
import org.eclipse.tractusx.sde.common.utils.LogUtil;
import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.Criterion;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PCFExchangeAssetUtils {
	
	private final EDCAssetLookUp edcAssetLookUp;
	
	private final PCFAssetStaticPropertyHolder pcfAssetStaticPropertyHolder;
	
	@Cacheable(value = "bpn-pcfexchange", key = "#bpnNumber")
	public List<QueryDataOfferModel> getPCFExchangeUrl(String bpnNumber) {
		return edcAssetLookUp.getEDCAssetsByType(bpnNumber, getFilterCriteria());
	}

	@CacheEvict(value = "bpn-pcfexchange", key = "#bpnNumber")
	public void removePCFExchangeCache(String bpnNumber) {
		log.info(LogUtil.encode("Cleared '" + bpnNumber + "' bpn-pcfexchange cache"));
	}

	@CacheEvict(value = "bpn-pcfexchange", allEntries = true)
	public void clearePCFExchangeAllCache() {
		log.info("Cleared All bpn-pcfexchange cache");
	}
	
	private List<Criterion> getFilterCriteria() {

		return List.of(
				Criterion.builder()
				.operandLeft("'http://purl.org/dc/terms/type'.'@id'")
				.operator("=")
				.operandRight("https://w3id.org/catenax/taxonomy#"+pcfAssetStaticPropertyHolder.getAssetPropTypePCFExchangeType())
				.build());
	}

}
