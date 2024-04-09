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

package org.eclipse.tractusx.sde.digitaltwins.gateways.external;

import java.net.URI;

import org.eclipse.tractusx.sde.common.utils.TokenUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

public class DigitalTwinsFeignClientConfiguration {

	@Bean(name = "digitalTwinsFeignClientConfigurationInterceptor")
	public DigitalTwinsFeignClientConfigurationInterceptor appRequestInterceptor() {
		return new DigitalTwinsFeignClientConfigurationInterceptor();
	}
}

@Slf4j
class DigitalTwinsFeignClientConfigurationInterceptor implements RequestInterceptor {

	@Value(value = "${digital-twins.authentication.clientSecret}")
	private String digitalAppClientSecret;

	@Value(value = "${digital-twins.authentication.clientId}")
	private String digitalAppClientId;

	@Value(value = "${digital-twins.authentication.grantType}")
	private String digitalGrantType;

	@Value(value = "${digital-twins.authentication.url:default}")
	private URI digitalAppTokenURI;
	
	@Value(value = "${digital-twins.authentication.scope:}")
	private String digitalTwinsScope;
	
	@Value(value = "${digital-twins.managed.thirdparty:false}")
	private boolean dDTRManagedThirdparty;

	@Autowired
	private TokenUtility tokenUtilityForDigital;
	
	private String accessTokenForDigital;
	
	@Override
	public void apply(RequestTemplate template) {
		template.header("Authorization", getTokenForDigital());
		log.debug("Bearer authentication applied for DigitalTwinsFeignClientConfigurationInterceptor");
	}

	@SneakyThrows
	public String getTokenForDigital() {
		if (accessTokenForDigital != null && tokenUtilityForDigital.isTokenValid(accessTokenForDigital)) {
			return "Bearer " + accessTokenForDigital;
		}
	
		if(dDTRManagedThirdparty) {
			accessTokenForDigital = tokenUtilityForDigital.getToken(digitalAppTokenURI, digitalGrantType, digitalAppClientId, digitalAppClientSecret, digitalTwinsScope);
		}else {
			accessTokenForDigital = tokenUtilityForDigital.getToken(digitalAppTokenURI, digitalGrantType, digitalAppClientId, digitalAppClientSecret);
		}
		return "Bearer " + accessTokenForDigital;
	}

}
