/********************************************************************************
 * Copyright (c) 2024 T-Systems International GmbH
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.sde.policyhub.api;

import java.net.URI;

import org.eclipse.tractusx.sde.common.utils.TokenUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

public class PolicyHubExternalServiceApi {
	
	@Bean(name = "policyHubExternalServiceApiInterceptor")
	public PolicyHubExternalServiceApiInterceptor appRequestInterceptor() {
		return new PolicyHubExternalServiceApiInterceptor();
	}
}

@Slf4j
class PolicyHubExternalServiceApiInterceptor implements RequestInterceptor {
	
	@Value(value = "${policy.hub.authentication.url}")
	private URI policyHubTokenURI;

	@Value(value = "${policy.hub.clientId}")
	private String policyHubClientId;

	@Value(value = "${policy.hub.clientSecret}")
	private String policyHubClientSecret;

	@Value(value = "${policy.hub.grantType}")
	private String policyHubGrantType;

	@Autowired
	private TokenUtility tokenUtilityforBpn;

	private String policyHubAccessToken;

	@Override
	public void apply(RequestTemplate template) {
		template.header("Authorization", getTokenForBPN());
		log.debug("Bearer authentication applied for PolicyHubExternalServiceApiInterceptor");
	}

	@SneakyThrows
	public String getTokenForBPN() {
		if (policyHubAccessToken != null && tokenUtilityforBpn.isTokenValid(policyHubAccessToken)) {
			return "Bearer " + policyHubAccessToken;
		}
		policyHubAccessToken = tokenUtilityforBpn.getToken(policyHubTokenURI, policyHubGrantType, policyHubClientId, policyHubClientSecret);
		return "Bearer " + policyHubAccessToken;
	}
	
}
