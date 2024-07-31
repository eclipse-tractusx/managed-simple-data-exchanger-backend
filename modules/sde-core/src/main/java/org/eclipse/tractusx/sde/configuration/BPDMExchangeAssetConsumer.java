/********************************************************************************
 * Copyright (c) 2023,2024 T-Systems International GmbH
 * Copyright (c) 2023,2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.configuration;

import java.util.List;

import org.eclipse.tractusx.sde.core.service.PartnerPoolService;
import org.eclipse.tractusx.sde.portal.model.response.LegalEntityResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("default")
public class BPDMExchangeAssetConsumer {

	private final PartnerPoolService partnerPoolService;
	
	@PostConstruct
	@SneakyThrows
	public void init() {
		try {
			List<LegalEntityResponse> legalEntitiesResponse = partnerPoolService.fetchLegalEntitiesData(null, "test", 0,
					10);
			log.info("BPDM service ready to use, 'test' company found leagal entity =>" + legalEntitiesResponse.size());
		} catch (Exception e) {
			log.error("Unable to perform auto negotiation for BPDM service for legal entiry search");
		}
	}

}