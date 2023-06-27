/********************************************************************************
 * Copyright (c) 2023 T-Systems International GmbH
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.sde.bpndiscovery.utils;

import java.net.URI;
import java.util.Base64;

import org.eclipse.tractusx.sde.bpndiscovery.api.IBpndiscoveryExternalServiceApi;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Component
@RequiredArgsConstructor
public class BpnDiscoveryAuthToken {

	private final IBpndiscoveryExternalServiceApi bpndiscoveryExternalServiceApi;

	private static final String CLIENT_ID = "client_id";
	private static final String CLIENT_SECRET = "client_secret";
	private static final String GRANT_TYPE = "grant_type";

	@Value(value = "${discovery.authentication.url}")
	private URI authTokenUrl;

	@Value(value = "${discovery.clientId}")
	private String clientId;

	@Value(value = "${discovery.clientSecret}")
	private String clientSecret;

	@Value(value = "${discovery.grantType}")
	private String grantType;

	private String accessToken;

	@SneakyThrows
	public String getToken() {
		try {
			if (accessToken != null && isTokenValid()) {
				return "Bearer " + accessToken;
			}

			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add(GRANT_TYPE, grantType);
			body.add(CLIENT_ID, clientId);
			body.add(CLIENT_SECRET, clientSecret);

			var resultBody = bpndiscoveryExternalServiceApi.getBpnDiscoveryAuthToken(authTokenUrl, body);

			if (resultBody != null) {
				accessToken = resultBody.getAccessToken();
				return "Bearer " + accessToken;
			}
		} catch (Exception e) {
			throw new ServiceException("Unable to process auth request: " + authTokenUrl + ", " + e.getMessage());
		}
		return null;
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
