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

package org.eclipse.tractusx.sde.edc.api;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.edc.model.contractnegotiation.AcknowledgementId;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedByIdResponse;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.JsonNode;

@FeignClient(value = "EDRApiProxy", url = "placeholder")
public interface EDRApiProxy {

	@PostMapping(path = "/v2/edrs", consumes = MediaType.APPLICATION_JSON_VALUE)
	AcknowledgementId edrCacheCreate(URI url, @RequestBody JsonNode requestBody,
			@RequestHeader Map<String, String> requestHeader);

	@PostMapping(path = "/v2/edrs/request", consumes = MediaType.APPLICATION_JSON_VALUE)
	List<EDRCachedResponse> getEDRCachedByAsset(URI url, @RequestBody JsonNode requestBody,
			@RequestHeader Map<String, String> requestHeader);

	@GetMapping(path = "/v2/edrs/{transferProcessId}/dataaddress")
	EDRCachedByIdResponse getEDRCachedByTransferProcessId(URI url,
			@PathVariable("transferProcessId") String transferProcessId, @RequestParam("auto_refresh") boolean autoRefresh,
			@RequestHeader Map<String, String> requestHeader);

	@GetMapping
	Object getActualDataFromProviderDataPlane(URI url, @RequestHeader Map<String, String> requestHeader);

}