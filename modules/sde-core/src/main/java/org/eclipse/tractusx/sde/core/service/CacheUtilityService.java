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

import java.util.List;

import org.eclipse.tractusx.sde.edc.util.UtilityFunctions;
import org.eclipse.tractusx.sde.portal.api.IPortalExternalServiceApi;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheUtilityService {
	
	
	private final IPortalExternalServiceApi portalExternalServiceApi;
	
	@Cacheable("memberCompaniesList") 
	public List<String> getPartner() {
		String token = UtilityFunctions.getAuthToken();
		log.info("Refreshed bpn fetch member companies data list");
		return portalExternalServiceApi.fetchMemberCompaniesData(token);
	}

	@CacheEvict(value = "memberCompaniesList", allEntries = true)
	@Scheduled(fixedRateString = "1000")
	public void removeAllBPNNumberCache() {
		log.info("All member companies BPN cache removed from cache");
	}

}
