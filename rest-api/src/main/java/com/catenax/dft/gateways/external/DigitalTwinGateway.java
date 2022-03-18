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

package com.catenax.dft.gateways.external;

import com.catenax.dft.entities.digitalTwins.AssetAdministrationShellDescriptor;
import com.catenax.dft.entities.digitalTwins.LookupRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@Slf4j
public class DigitalTwinGateway {

    private final String DIGITAL_TWINS_URL = "https://catenaxintakssrv.germanywestcentral.cloudapp.azure.com/semantics";
    private final String TOKEN_URL = "https://catenaxintakssrv.germanywestcentral.cloudapp.azure.com/iamcentralidp/auth/realms/CX-Central/protocol/openid-connect/token";

    @SneakyThrows
    public List<String> getDigitalTwins(LookupRequest lookupRequest) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", getToken()));
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>( headers);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("assetIds",lookupRequest.getAssetIds());
        ResponseEntity<String> response = restTemplate.exchange(DIGITAL_TWINS_URL + "/lookup/shells",HttpMethod.GET, String.class);

        log.info(response.getBody());
        return null;
    }

    public void createDigitalTwin(AssetAdministrationShellDescriptor aasDescriptor) {

    }

    @SneakyThrows
    private String getToken() {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", "sa-cl6-cx-4");
        map.add("client_secret", "VrL8uSG5Tn3NrFiY39vs0klTmlvsRRmo");
        map.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);


        ResponseEntity<String> response = restTemplate.exchange(TOKEN_URL, HttpMethod.POST, entity, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response.getBody());
        String token = node.path("access_token").asText();

        return  token;
    }
}
