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

import com.catenax.dft.entities.digitalTwins.request.CreateSubModelRequest;
import com.catenax.dft.entities.digitalTwins.request.ShellDescriptorRequest;
import com.catenax.dft.entities.digitalTwins.request.ShellLookupRequest;
import com.catenax.dft.entities.digitalTwins.response.ShellDescriptorResponse;
import com.catenax.dft.entities.digitalTwins.response.ShellLookupResponse;
import com.catenax.dft.entities.digitalTwins.response.SubModelListResponse;
import com.catenax.dft.entities.digitalTwins.response.SubModelResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class DigitalTwinGateway {

    public static final String AUTHORIZATION = "Authorization";
    public static final String ASSET_IDS_QUERY_PARAMETER = "assetIds";
    public static final String CLIENT_ID_TOKEN_QUERY_PARAMETER = "client_id";
    public static final String CLIENT_SECRET_TOKEN_QUERY_PARAMETER = "client_secret";
    public static final String GRANT_TYPE_TOKEN_QUERY_PARAMETER = "grant_type";
    public static final String CLIENT_CREDENTIALS_TOKEN_QUERY_PARAMETER_VALUE = "client_credentials";
    public static final String ACCESS_TOKEN = "access_token";

    @Value(value = "${digital-twins.authentication.clientSecret}")
    private String clientSecret;
    @Value(value = "${digital-twins.authentication.clientId}")
    private String clientId;
    @Value(value = "${digital-twins.url}")
    private String digitalTwinsUrl;
    @Value(value = "${digital-twins.authentication.url}")
    private String tokenUrl;

    @SneakyThrows
    public ShellLookupResponse shellLookup(ShellLookupRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, getBearerToken());
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);

        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put(ASSET_IDS_QUERY_PARAMETER, request.toJsonString());

        String url = digitalTwinsUrl + "/lookup/shells";
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

        log.debug("[DigitalTwinGateway][Shell LookUp] Url: " + urlTemplate);
        log.debug("[DigitalTwinGateway][Shell LookUp] Request: " + request.toJsonString());

        ShellLookupResponse responseBody;
        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Unable to find shells");
            responseBody = new ShellLookupResponse();
        } else {
            responseBody = response.getBody();
        }
        return responseBody;
    }

    public ShellDescriptorResponse createShellDescriptor(ShellDescriptorRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, getBearerToken());
        HttpEntity<ShellDescriptorRequest> entity = new HttpEntity<>(request, headers);

        String url = digitalTwinsUrl + "/registry/shell-descriptors";
        ResponseEntity<ShellDescriptorResponse> response = restTemplate.postForEntity(url, entity, ShellDescriptorResponse.class);
        log.debug("[DigitalTwinGateway][Create Shell Descriptor] Url: " + url);
        log.debug("[DigitalTwinGateway][Create Shell Descriptor] Request: " + request.toJsonString());

        ShellDescriptorResponse responseBody;
        if (response.getStatusCode() != HttpStatus.CREATED) {
            log.error("Unable to create shell descriptor");
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

        String url = String.format(digitalTwinsUrl + "/registry/shell-descriptors/%s/submodel-descriptors", shellId);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        log.debug("[DigitalTwinGateway][Create SubModel] Url: " + url);
        log.debug("[DigitalTwinGateway][Create SubModel] Request: " + request.toJsonString());

        if (response.getStatusCode() != HttpStatus.CREATED) {
            log.error("Unable to create shell descriptor");
        }
    }

    @SneakyThrows
    public SubModelListResponse getSubModel(String shellId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, getBearerToken());
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);

        String url = String.format(digitalTwinsUrl + "/registry/shell-descriptors/%s/submodel-descriptors", shellId);

        ResponseEntity<SubModelListResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                SubModelListResponse.class);

        log.debug("[DigitalTwinGateway][Get SubModel] Url: " + url);

        SubModelListResponse responseBody = null;
        if (response.getStatusCode() == HttpStatus.OK) {
            responseBody = response.getBody();
        }
        return responseBody;
    }

    @SneakyThrows
    private String getBearerToken() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(CLIENT_ID_TOKEN_QUERY_PARAMETER, clientId);
        map.add(CLIENT_SECRET_TOKEN_QUERY_PARAMETER, clientSecret);
        map.add(GRANT_TYPE_TOKEN_QUERY_PARAMETER, CLIENT_CREDENTIALS_TOKEN_QUERY_PARAMETER_VALUE);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response.getBody());

        String accessToken = node.path(ACCESS_TOKEN).asText();
        return String.format("Bearer %s", accessToken);
    }

}
