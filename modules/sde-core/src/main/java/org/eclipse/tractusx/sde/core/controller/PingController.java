/********************************************************************************
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022,2024 T-Systems International GmbH
 * Copyright (c) 2022,2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.core.controller;

import static org.springframework.http.ResponseEntity.ok;

import java.time.LocalDateTime;

import org.eclipse.tractusx.sde.edc.util.EDCAssetUrlCacheService;
import org.eclipse.tractusx.sde.portal.utils.MemberCompanyBPNCacheUtilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PingController {

	private final EDCAssetUrlCacheService edcAssetUrlCacheService;

	private final MemberCompanyBPNCacheUtilityService memberCompanyBPNCacheUtilityService;

	@GetMapping(value = "/ping")
	public ResponseEntity<String> getProcessReportById() {
		return ok().body(LocalDateTime.now().toString());
	}

	@GetMapping(value = "/cache/clear-memebercompany-bpnnumber")
	public ResponseEntity<String> clearBpnnumberCache() {
		memberCompanyBPNCacheUtilityService.removeAllBPNNumberCache();
		return ok().body("Cleared");
	}

	@GetMapping(value = "/cache/clear-ddtrurl")
	public ResponseEntity<String> clearDdtrurlCache() {
		edcAssetUrlCacheService.clearDDTRUrlCache();
		return ok().body("Cleared");
	}
	
	@GetMapping(value = "/cache/clear-pcfurl")
	public ResponseEntity<String> clearPCFExchangeUrlCache() {
		edcAssetUrlCacheService.clearPCFExchangeUrlCache();
		return ok().body("Cleared");
	}
}