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

package org.eclipse.tractusx.sde.bpndiscovery.api;

import java.net.URI;

import org.eclipse.tractusx.sde.common.utils.TokenUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

public class BpndiscoveryExternalServiceApi {

	@Bean(name = "portalExternalServiceApiInterceptor")
	public BpndiscoveryExternalServiceApiInterceptor appRequestInterceptor() {
		return new BpndiscoveryExternalServiceApiInterceptor();
	}
}

@Slf4j
class BpndiscoveryExternalServiceApiInterceptor implements RequestInterceptor {

	@Value(value = "${discovery.authentication.url}")
	private URI bpnAppTokenURI;

	@Value(value = "${discovery.clientId}")
	private String bpnAppClientId;

	@Value(value = "${discovery.clientSecret}")
	private String bpnAppClientSecret;

	@Value(value = "${discovery.grantType}")
	private String bpnGrantType;

	@Autowired
	private TokenUtility tokenUtilityforBpn;

	private String bpnAccessToken;

	@Override
	public void apply(RequestTemplate template) {
		template.header("Authorization", getTokenForBPN());
		log.debug("Bearer authentication applied for PortalExternalServiceApiInterceptor");
	}

	@SneakyThrows
	public String getTokenForBPN() {
		if (bpnAccessToken != null && tokenUtilityforBpn.isTokenValid(bpnAccessToken)) {
			return "Bearer " + bpnAccessToken;
		}
		bpnAccessToken = tokenUtilityforBpn.getToken(bpnAppTokenURI, bpnGrantType, bpnAppClientId, bpnAppClientSecret);
		return "Bearer " + bpnAccessToken;
	}

}