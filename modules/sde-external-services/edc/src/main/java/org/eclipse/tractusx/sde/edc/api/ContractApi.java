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

package org.eclipse.tractusx.sde.edc.api;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.edc.model.contractnegotiation.AcknowledgementId;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractAgreementDto;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractNegotiationDto;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractNegotiations;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ContractApi", url = "placeholder")
public interface ContractApi {

	@PostMapping(path = "/data/contractnegotiations", consumes = MediaType.APPLICATION_JSON_VALUE)
	AcknowledgementId contractnegotiations(URI url, @RequestBody ContractNegotiations requestBody,
			@RequestHeader Map<String, String> requestHeader);

	@GetMapping(path = "/data/contractnegotiations/{contractnegotiationsId}")
	ContractNegotiationDto getContractDetails(URI url,
			@PathVariable("contractnegotiationsId") String contractnegotiationsId,
			@RequestHeader Map<String, String> requestHeader);

	@GetMapping(path = "/data/contractnegotiations")
	List<ContractNegotiationDto> getAllContractNegotiations(URI url, @RequestParam("limit") Integer limit,
			@RequestParam("offset") Integer offset, @RequestHeader Map<String, String> requestHeader);

	@GetMapping(path = "/data/contractnegotiations/{contractnegotiationsId}/agreement")
	ContractAgreementDto getAgreementBasedOnNegotiationId(URI url,
			@PathVariable("contractnegotiationsId") String contractnegotiationsId,
			@RequestHeader Map<String, String> requestHeader);
	
	@PostMapping(path = "/data/contractnegotiations/{contractnegotiationsId}/decline", consumes = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> declineContract(URI url,
			@PathVariable("contractnegotiationsId") String contractnegotiationsId,
			@RequestHeader Map<String, String> requestHeader);
	
	@PostMapping(path = "/data/contractnegotiations/{contractnegotiationsId}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> cancelContract(URI url,
			@PathVariable("contractnegotiationsId") String contractnegotiationsId,
			@RequestHeader Map<String, String> requestHeader);

}
