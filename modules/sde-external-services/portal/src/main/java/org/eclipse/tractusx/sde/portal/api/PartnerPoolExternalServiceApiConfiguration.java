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

package org.eclipse.tractusx.sde.portal.api;

import java.net.URI;
import java.util.Base64;

import org.eclipse.tractusx.sde.portal.utils.TokenUtility;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

public class PartnerPoolExternalServiceApiConfiguration {

	@Bean(name = "partnerPoolExternalServiceApiInterceptor")
	public PartnerPoolExternalServiceApiInterceptor appRequestInterceptor() {
		return new PartnerPoolExternalServiceApiInterceptor();
	}
}

@Slf4j
class PartnerPoolExternalServiceApiInterceptor implements RequestInterceptor {

	@Value(value = "${partner.pool.authentication.url}")
	private URI appTokenURI;

	@Value(value = "${partner.pool.clientSecret}")
	private String appClientSecret;

	@Value(value = "${partner.pool.clientId}")
	private String appClientId;


	@Autowired
	private TokenUtility tokenUtility;

	private String accessToken;

	@Override
	public void apply(RequestTemplate template) {
		template.header("Authorization", getToken());
		log.debug("Bearer authentication applied for PartnerPoolExternalServiceApiInterceptor");
	}

	@SneakyThrows
	public String getToken() {
		try {
			if (accessToken != null && isTokenValid()) {
				return "Bearer " + accessToken;
			}
			accessToken = tokenUtility.getValidJWTTokenforAppTechUser(appTokenURI, appClientId, appClientSecret);
			return "Bearer " + accessToken;
		} catch (Exception e) {
			throw new ServiceException("Unable to process auth request: " + appTokenURI + ", " + e.getMessage());
		}
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