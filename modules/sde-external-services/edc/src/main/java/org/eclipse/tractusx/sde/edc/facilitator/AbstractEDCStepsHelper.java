/********************************************************************************
 * Copyright (c) 2022, 2023 T-Systems International GmbH
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.edc.facilitator;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

public class AbstractEDCStepsHelper {

	@Value("${edc.consumer.hostname}${edc.consumer.managementpath:/data}${edc.consumer.managementpath.apiversion:/v2}")
	protected String consumerHost;

	@Value("${edc.consumer.hostname}${edc.consumer.managementpath:/data}")
	protected String consumerHostWithDataPath;

	@Value("${edc.consumer.hostname}")
	protected String consumerHostWithoutDataPath;

	@Value("${edc.consumer.apikeyheader}")
	private String edcApiKeyHeader;

	@Value("${edc.consumer.apikey}")
	private String edcApiKeyValue;

	@Value("${edc.hostname}${edc.managementpath:/data}${edc.managementpath.apiversion:/v2}")
	protected String providerHost;

	@Value("${edc.hostname}${edc.managementpath:/data}")
	protected String providerHostWithManagementPath;

	@Value("${edc.hostname}")
	protected String providerHostWithoutDataPath;

	@Value("${edc.consumer.protocol.path:/api/v1/dsp}")
	protected String protocolPath;
	
	@Value("${edc.consumer.protocol.path.append:true}")
	protected boolean appendProtocolPath;

	@Value("${edc.apiKeyHeader}")
	private String edcProviderApiKeyHeader;

	@Value("${edc.apiKey}")
	private String edcProviderApiKeyValue;

	public Map<String, String> getAuthHeader() {
		Map<String, String> requestHeader = new HashMap<>();
		requestHeader.put(edcApiKeyHeader, edcApiKeyValue);
		return requestHeader;
	}

	public Map<String, String> getProviderAuthHeader() {
		Map<String, String> requestHeader = new HashMap<>();
		requestHeader.put(edcProviderApiKeyHeader, edcProviderApiKeyValue);
		return requestHeader;
	}

}