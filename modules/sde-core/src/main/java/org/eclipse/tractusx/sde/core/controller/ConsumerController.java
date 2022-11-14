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

package org.eclipse.tractusx.sde.core.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractAgreementResponse;
import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.edc.services.ConsumerControlPanelService;
import org.eclipse.tractusx.sde.portal.model.ConnectorInfo;
import org.eclipse.tractusx.sde.portal.model.response.LegalEntityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ConsumerController {


    @Autowired
    private final ConsumerControlPanelService consumerControlPanelService;

    public ConsumerController(ConsumerControlPanelService consumerControlPanelService) {
        this.consumerControlPanelService = consumerControlPanelService;
    }

    @GetMapping(value = "/query-data-offers")
    public ResponseEntity<Object> queryOnDataOffers(@RequestParam String providerUrl)
            throws Exception {
        log.info("Request received : /api/query-data-Offers");
        return ok().body(consumerControlPanelService.queryOnDataOffers(providerUrl));
    }

    @PostMapping(value = "/subscribe-data-offers")
    public ResponseEntity<Object> subscribeDataOffers(@Valid @RequestBody ConsumerRequest consumerRequest) {
        String processId = UUID.randomUUID().toString();
        log.info("Request recevied : /api/subscribe-data-offers");
        consumerControlPanelService.subscribeDataOffers(consumerRequest, processId);
        return ResponseEntity.ok().body(processId);
    }

    @GetMapping(value = "/contract-offers", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> queryOnDataOffersStatus(@RequestParam(value = "limit", required = false) Integer limit, @RequestParam(value = "offset", required = false) Integer offset) {
        log.info("Request received : /api/contract-offer");
        if (limit == null) {
            limit = 10;
        }
        if (offset == null) {
            offset = 1;
        }
        List<ContractAgreementResponse> responseEntity = consumerControlPanelService.getAllContractOffers(limit, offset);
        return ok().body(responseEntity);
    }

    @GetMapping(value = "/legal-entities")
    public ResponseEntity<LegalEntityResponse[]> fetchLegalEntitiesData(@RequestParam String searchText, @RequestParam Integer page, @RequestParam Integer size)
            throws Exception {
        log.info("Request received : /api/legal-entities");
        return consumerControlPanelService.fetchLegalEntitiesData(searchText, page, size);
    }

    @PostMapping(value = "/connectors-discovery")
    public ResponseEntity<ConnectorInfo[]> fetchConnectorInfo(@RequestBody String[] bpns)
            throws Exception {
        log.info("Request received : /api/connectors-discovery");
        return consumerControlPanelService.fetchConnectorInfo(bpns);
    }
}
