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

package com.catenax.dft.api;

import com.catenax.dft.model.contractoffers.ContractOffersCatalogResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(value = "ContractOfferCatalogApi", url = "${edc.consumer.hostname}")
public interface ContractOfferCatalogApi {
    @GetMapping(value = "/data/catalog")
    public ContractOffersCatalogResponse getContractOffersCatalog(
            @RequestHeader Map<String, String> requestHeader,
            @RequestParam String providerUrl
    );
}
