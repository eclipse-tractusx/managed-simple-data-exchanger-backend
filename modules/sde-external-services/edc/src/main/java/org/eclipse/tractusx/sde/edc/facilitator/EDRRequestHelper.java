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

package org.eclipse.tractusx.sde.edc.facilitator;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.edc.api.EDRApiProxy;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ActionRequest;
import org.eclipse.tractusx.sde.edc.mapper.ContractMapper;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.AcknowledgementId;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractNegotiations;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedByIdResponse;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedResponse;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class EDRRequestHelper extends AbstractEDCStepsHelper {

	private final EDRApiProxy edrApiProxy;
	private final ContractMapper contractMapper;

	@SneakyThrows
	public String edrRequestInitiate(String providerUrl, String providerId, String offerId, String assetId,
			ActionRequest action, Map<String, String> extensibleProperty) {

		ContractNegotiations contractNegotiations = contractMapper
				.prepareContractNegotiations(providerUrl + protocolPath, offerId, assetId, providerId, action);

		AcknowledgementId acknowledgementId = edrApiProxy.edrCacheCreate(new URI(consumerHostWithDataPath),
				contractNegotiations, getAuthHeader());
		return acknowledgementId.getId();
	}

	@SneakyThrows
	public List<EDRCachedResponse> getEDRCachedByAsset(String assetId) {
		return edrApiProxy.getEDRCachedByAsset(new URI(consumerHostWithDataPath), assetId, getAuthHeader());
	}

	@SneakyThrows
	public EDRCachedByIdResponse getEDRCachedByTransferProcessId(String transferProcessId) {
		return edrApiProxy.getEDRCachedByTransferProcessId(new URI(consumerHostWithDataPath), transferProcessId,
				getAuthHeader());
	}

	@SneakyThrows
	public Object getDataFromProvider(EDRCachedByIdResponse authorizationToken, String downloadDataAs) {
		Map<String, String> authHeader = new HashMap<>();
		authHeader.put(authorizationToken.getAuthKey(), authorizationToken.getAuthCode());
		return edrApiProxy.getActualDataFromProviderDataPlane(
				new URI(authorizationToken.getEndpoint()), downloadDataAs, authHeader);
	}

}
