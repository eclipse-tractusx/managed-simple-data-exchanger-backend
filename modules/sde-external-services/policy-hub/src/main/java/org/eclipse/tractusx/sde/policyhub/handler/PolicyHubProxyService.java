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
package org.eclipse.tractusx.sde.policyhub.handler;

import java.util.List;

import org.eclipse.tractusx.sde.policyhub.api.IPolicyHubExternalServiceApi;
import org.eclipse.tractusx.sde.policyhub.model.request.PolicyContentRequest;
import org.eclipse.tractusx.sde.policyhub.model.response.PolicyResponse;
import org.eclipse.tractusx.sde.policyhub.model.response.PolicyTypeResponse;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class PolicyHubProxyService implements IPolicyHubProxyService {
	
	private final IPolicyHubExternalServiceApi policyHubExternalServiceApi;

	@Override
	@SneakyThrows
	public List<String> getPolicyAttributes() {
		return policyHubExternalServiceApi.getPolicyAttributes();
	}

	@Override
	@SneakyThrows
	public List<PolicyTypeResponse> getPolicyTypes(String type, String useCase) {
		return policyHubExternalServiceApi.getPolicyTypes(type, useCase);
	}

	@Override
	@SneakyThrows
	public PolicyResponse getPolicyContent(String useCase, String type, String credential, String operatorId,
			String value) {
		return policyHubExternalServiceApi.getPolicyContent(useCase, type, credential, operatorId, value);
	}

	@Override
	@SneakyThrows
	public PolicyResponse getPolicyContent(PolicyContentRequest policyContentRequest) {
		return policyHubExternalServiceApi.getPolicyContent(policyContentRequest);
	}

}
