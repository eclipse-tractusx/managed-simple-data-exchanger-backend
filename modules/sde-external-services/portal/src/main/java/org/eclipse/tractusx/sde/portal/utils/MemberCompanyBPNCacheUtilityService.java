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
package org.eclipse.tractusx.sde.portal.utils;

import java.util.List;

import org.eclipse.tractusx.sde.portal.api.IPortalExternalServiceApi;
import org.hibernate.service.spi.ServiceException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberCompanyBPNCacheUtilityService {

	private final IPortalExternalServiceApi portalExternalServiceApi;

	@Cacheable("memberCompaniesList")
	public List<String> getAllPartners(String accessToken) throws ServiceException {
		log.info("Refreshed bpn fetch member companies data list");
		List<String> fetchMemberCompaniesData = null;
		try {
			fetchMemberCompaniesData = portalExternalServiceApi.fetchMemberCompaniesData(accessToken);
		} catch (Exception e) {
			log.error(accessToken);
			log.error(e.getMessage());
			throw new ServiceException(e.getMessage());
		}
		return fetchMemberCompaniesData;
	}

	@CacheEvict(value = "memberCompaniesList", allEntries = true)
	@Scheduled(fixedRateString = "3600000")
	public void removeAllBPNNumberCache() {
		log.info("All member companies BPN cache removed from cache");
	}

}
