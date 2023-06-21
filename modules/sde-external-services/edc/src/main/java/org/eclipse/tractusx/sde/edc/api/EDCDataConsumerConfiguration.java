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

package org.eclipse.tractusx.sde.edc.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

public class EDCDataConsumerConfiguration {

	@Bean(name = "edcDataConsumerInterceptor")
	public EDCDataConsumerInterceptor appRequestInterceptor() {
		return new EDCDataConsumerInterceptor();
	}
}

@Slf4j
class EDCDataConsumerInterceptor implements RequestInterceptor {

	@Value(value = "${edc.consumer.apikeyheader}")
	private String apiKeyHeader;

	@Value(value = "${edc.consumer.apikey}")
	private String apiKey;

	@Override
	public void apply(RequestTemplate template) {
		template.header(apiKeyHeader, apiKey);
		template.header("Content-Type: application/json");
		log.debug("Bearer authentication applied for EDCDataConsumerInterceptor");
	}

}