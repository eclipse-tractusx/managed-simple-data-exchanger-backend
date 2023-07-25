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
package org.eclipse.tractusx.sde.bpndiscovery.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.bpndiscovery.model.request.BpnDiscoveryRequest;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BPNDiscoveryUseCaseHandler extends Step {

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

}
