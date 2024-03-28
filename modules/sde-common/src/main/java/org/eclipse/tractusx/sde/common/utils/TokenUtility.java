/********************************************************************************
 * Copyright (c) 2023, 2024 T-Systems International GmbH
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.common.utils;

import java.net.URI;
import java.util.Base64;

import org.hibernate.service.spi.ServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenUtility {

	private static final String CLIENT_ID = "client_id";
	private static final String CLIENT_SECRET = "client_secret";
	private static final String GRANT_TYPE = "grant_type";
	private static final String SCOPE = "scope";

	private final ITokenUtilityProxy tokenUtilityProxy;

	@SneakyThrows
	public String getToken(URI appTokenURI, String grantType, String appClientId, String appClientSecret) {

		MultiValueMap<String, Object> body = getMultiValueBody(grantType, appClientId, appClientSecret);

		return createToken(appTokenURI, body);

	}

	@SneakyThrows
	public String getToken(URI appTokenURI, String grantType, String appClientId, String appClientSecret,
			String scope) {

		MultiValueMap<String, Object> body = getMultiValueBody(grantType, appClientId, appClientSecret);
		body.add(SCOPE, scope);

		return createToken(appTokenURI, body);

	}

	private MultiValueMap<String, Object> getMultiValueBody(String grantType, String appClientId,
			String appClientSecret) {
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add(GRANT_TYPE, grantType);
		body.add(CLIENT_ID, appClientId);
		body.add(CLIENT_SECRET, appClientSecret);
		return body;
	}

	private String createToken(URI appTokenURI, MultiValueMap<String, Object> body) {

		try {
			var resultBody = tokenUtilityProxy.getToken(appTokenURI, body);

			if (resultBody != null)
				return resultBody.getAccessToken();
			else
				throw new ServiceException(
						"Unable to get auth token because auth response resultBody is: " + resultBody);
		} catch (FeignException e) {
			log.error("FeignException RequestBody : " + e.request());
			String errorMsg = "Error in DT twin lookup " + e.request().url() + ", because: " + body+ "," + e.contentUTF8();
			log.error("FeignException : " + errorMsg);
			throw new ServiceException(errorMsg);
		} catch (Exception e) {
			throw new ServiceException("Unable to process auth request: " + appTokenURI + ", " + e.getMessage());
		}
	}

	public String getOriginalRequestAuthToken() throws ServiceException {
		ServletRequestAttributes reqAtt = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (reqAtt != null) {
			return reqAtt.getRequest().getHeader("Authorization");
		} else {
			throw new ServiceException("Auth token is not present");
		}
	}

	@SneakyThrows
	public boolean isTokenValid(String accessToken) {
		String[] str = accessToken.split("\\.");
		Base64.Decoder decoder = Base64.getUrlDecoder();
		String body = new String(decoder.decode(str[1]));

		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(body);
		long tokenExpirationTime = actualObj.get("exp").asLong() * 1000;
		long currentTime = System.currentTimeMillis();

		return tokenExpirationTime - 40000 > currentTime;
	}

}
