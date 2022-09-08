/*
 * Copyright 2022 CatenaX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.catenax.dft.controllers;

import static org.springframework.http.ResponseEntity.ok;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.catenax.dft.service.ConsumerControlPanelService;

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
        log.info("Request received : /api/v1/query-data-Offers");
        return ok().body(consumerControlPanelService.queryOnDataOffers(providerUrl));
    }

}
