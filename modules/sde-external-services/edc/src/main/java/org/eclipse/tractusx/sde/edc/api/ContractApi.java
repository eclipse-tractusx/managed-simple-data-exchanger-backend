/********************************************************************************
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import org.eclipse.tractusx.sde.edc.model.contractnegotiation.AcknowledgementId;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractAgreementDto;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractNegotiationDto;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractNegotiations;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractNegotiationsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "ContractApi", url = "${edc.consumer.hostname}")
public interface ContractApi {

    @PostMapping(path = "/data/contractnegotiations", consumes = MediaType.APPLICATION_JSON_VALUE)
    AcknowledgementId contractnegotiations(@RequestBody ContractNegotiations requestBody,
                                           @RequestHeader Map<String, String> requestHeader);

    @GetMapping(path = "/data/contractnegotiations/{contractnegotiationsId}")
    ContractNegotiationsResponse checkContractNegotiationsStatus(@PathVariable("contractnegotiationsId") String contractnegotiationsId,
                                                                 @RequestHeader Map<String, String> requestHeader);

    @GetMapping(path = "/data/contractnegotiations")
    List<ContractNegotiationDto> getAllContractNegotiations(@RequestParam("limit") Integer limit, @RequestParam("offset") Integer offset,
                                                                  @RequestHeader Map<String, String> requestHeader);

    @GetMapping(path = "/data/contractnegotiations/{contractnegotiationsId}/agreement")
    ContractAgreementDto getAgreementBasedOnNegotiationId(@PathVariable("contractnegotiationsId") String contractnegotiationsId,
                                      @RequestHeader Map<String, String> requestHeader);

}
