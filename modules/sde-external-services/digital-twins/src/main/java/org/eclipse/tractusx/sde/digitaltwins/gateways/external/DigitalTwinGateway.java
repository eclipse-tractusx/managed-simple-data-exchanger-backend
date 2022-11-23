/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
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

package org.eclipse.tractusx.sde.digitaltwins.gateways.external;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tractusx.sde.digitaltwins.entities.request.CreateSubModelRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellDescriptorRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellLookupRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellLookupResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelListResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DigitalTwinGateway {

    private static final String AUTHORIZATION = "Authorization";
    private static final String ASSET_IDS_QUERY_PARAMETER = "assetIds";
    private static final String CLIENT_ID_TOKEN_QUERY_PARAMETER = "client_id";
    private static final String CLIENT_SECRET_TOKEN_QUERY_PARAMETER = "client_secret";
    private static final String GRANT_TYPE_TOKEN_QUERY_PARAMETER = "grant_type";
    private static final String ACCESS_TOKEN = "access_token";

    private String accessToken;

    @Value(value = "${digital-twins.authentication.clientSecret}")
    private String clientSecret;
    @Value(value = "${digital-twins.authentication.clientId}")
    private String clientId;
    @Value(value = "${digital-twins.authentication.grantType}")
    private String grantType;
    @Value(value = "${digital-twins.hostname}")
    private String digitalTwinsHost;
    @Value(value = "${digital-twins.authentication.url}")
    private String tokenUrl;

    public ShellLookupResponse shellLookup(ShellLookupRequest request) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, getBearerToken());
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);

        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put(ASSET_IDS_QUERY_PARAMETER, request.toJsonString());

        String url = digitalTwinsHost + "/lookup/shells";
        String urlTemplate = UriComponentsBuilder
                .fromHttpUrl(url)
                .queryParam(ASSET_IDS_QUERY_PARAMETER, "{assetIds}")
                .encode()
                .toUriString();

        ResponseEntity<ShellLookupResponse> response = restTemplate.exchange(
                urlTemplate,
                HttpMethod.GET,
                entity,
                ShellLookupResponse.class,
                queryParameters);

        ShellLookupResponse responseBody;
        if (response.getStatusCode() != HttpStatus.OK) {
            responseBody = new ShellLookupResponse();
        } else {
            responseBody = response.getBody();
        }
        return responseBody;
    }
    
    public String deleteShell(String assetId) {

        RestTemplate restTemplate = new RestTemplate();
        String deleteResponse = "";
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, getBearerToken());
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);

        String url = digitalTwinsHost + "/lookup/shells/";
        String urlTemplate = UriComponentsBuilder
                .fromHttpUrl(url)
                .path(assetId)
                .encode()
                .toUriString();

        ResponseEntity<Void> response = restTemplate.exchange(
                urlTemplate,
                HttpMethod.DELETE,
                entity,
                Void.class);

        if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
        	deleteResponse = "Asset identifier"+ assetId +"deleted successfully";
        }
        return deleteResponse;
    }

    public ShellDescriptorResponse createShellDescriptor(ShellDescriptorRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, getBearerToken());
        HttpEntity<ShellDescriptorRequest> entity = new HttpEntity<>(request, headers);
        String url = digitalTwinsHost + "/registry/shell-descriptors";
        ResponseEntity<ShellDescriptorResponse> response = restTemplate.postForEntity(url, entity, ShellDescriptorResponse.class);

        ShellDescriptorResponse responseBody;
        if (response.getStatusCode() != HttpStatus.CREATED) {
            responseBody = null;
        } else {
            responseBody = response.getBody();
        }
        return responseBody;
    }

    public void createSubModel(String shellId, CreateSubModelRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, getBearerToken());
        HttpEntity<CreateSubModelRequest> entity = new HttpEntity<>(request, headers);
        String url = digitalTwinsHost + "/registry/shell-descriptors/" + shellId + "/submodel-descriptors";
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() != HttpStatus.CREATED) {
            log.error("Unable to create shell descriptor");
        }
    }

    public SubModelListResponse getSubModels(String shellId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, getBearerToken());
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);
        String url = digitalTwinsHost + "/registry/shell-descriptors/" + shellId + "/submodel-descriptors";
        ResponseEntity<SubModelListResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                SubModelListResponse.class);

        SubModelListResponse responseBody = null;
        if (response.getStatusCode() == HttpStatus.OK) {
            responseBody = response.getBody();
        }
        return responseBody;
    }

    @SneakyThrows
    private String getBearerToken() {
        if (accessToken != null && isTokenValid()) {
            return "Bearer " + accessToken;
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(CLIENT_ID_TOKEN_QUERY_PARAMETER, clientId);
        map.add(CLIENT_SECRET_TOKEN_QUERY_PARAMETER, clientSecret);
        map.add(GRANT_TYPE_TOKEN_QUERY_PARAMETER, grantType);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, entity, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response.getBody());
        accessToken = node.path(ACCESS_TOKEN).asText();

        return "Bearer " + accessToken;
    }

    @SneakyThrows
    private boolean isTokenValid() {
        String[] str = accessToken.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String body = new String(decoder.decode(str[1]));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(body);
        long tokenExpirationTime = actualObj.get("exp").asLong() * 1000;
        long currentTime = System.currentTimeMillis();

        return tokenExpirationTime - 20000 > currentTime;
    }
}