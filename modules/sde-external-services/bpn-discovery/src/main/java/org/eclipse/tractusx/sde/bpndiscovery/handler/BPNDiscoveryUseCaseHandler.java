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
package org.eclipse.tractusx.sde.bpndiscovery.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.bpndiscovery.model.request.BpnDiscoveryRequest;
import org.eclipse.tractusx.sde.common.constants.SubmoduleCommonColumnsConstant;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.submodel.executor.BPNDiscoveryUsecaseStep;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.common.utils.JsonObjectUtility;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BPNDiscoveryUseCaseHandler extends Step implements BPNDiscoveryUsecaseStep {

	private final BpnDiscoveryProxyService bpnDiscoveryProxyService;

	public void run(Map<String, String> input) throws ServiceException {
		try {
			BpnDiscoveryRequest bpnDiscoveryRequest = new BpnDiscoveryRequest();
			List<BpnDiscoveryRequest> bpnDiscoveryKeyList = new ArrayList<>();

			input.entrySet().stream().forEach(e -> {
				bpnDiscoveryRequest.setType(e.getKey());
				bpnDiscoveryRequest.setKey(e.getValue());
				bpnDiscoveryKeyList.add(bpnDiscoveryRequest);
			});

			bpnDiscoveryProxyService.bpnDiscoveryBatchData(bpnDiscoveryKeyList);
		} catch (Exception e) {
			throw new ServiceException("Exception in BPN Discovery creation : " + e.getMessage());
		}
	}

	@Override
	public JsonNode run(Integer rowIndex, ObjectNode jsonObject, String processId, PolicyModel policy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(Integer rowIndex, JsonObject jsonObject, String delProcessId, String refProcessId) {
		// TODO Auto-generated method stub
		
	}

	public void run(JsonNode jsonObject) throws ServiceException {
		if (StringUtils.isBlank(
				JsonObjectUtility.getValueFromJsonObjectAsString(jsonObject, SubmoduleCommonColumnsConstant.UPDATED))) {
			try {
				BpnDiscoveryRequest bpnDiscoveryRequest = new BpnDiscoveryRequest();
				List<BpnDiscoveryRequest> bpnDiscoveryKeyList = new ArrayList<>();
				Map<String, String> input = generateBPNDiscoveryIdentifiersIds(jsonObject);
				input.entrySet().stream().forEach(e -> {
					bpnDiscoveryRequest.setType(e.getKey());
					bpnDiscoveryRequest.setKey(e.getValue());
					bpnDiscoveryKeyList.add(bpnDiscoveryRequest);
				});
				bpnDiscoveryProxyService.bpnDiscoveryBatchData(bpnDiscoveryKeyList);

			} catch (Exception e) {
				throw new ServiceException("Exception in BPN Discovery creation : " + e.getMessage());
			}
		}
	}

	private Map<String, String> generateBPNDiscoveryIdentifiersIds(JsonNode jsonObject) {
		return getBPNDiscoverySpecsOfModel().entrySet().stream().map(entry -> {
			String value = JsonObjectUtility.getValueFromJsonObjectAsString(jsonObject, entry.getValue().getAsString());
			if (StringUtils.isBlank(value)) {
				value = entry.getValue().getAsString();
			}
			return Map.entry(entry.getKey(), value);
		}).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	
}
