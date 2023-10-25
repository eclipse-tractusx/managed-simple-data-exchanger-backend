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

import java.util.List;

import org.eclipse.tractusx.sde.bpndiscovery.model.request.BpnDiscoveryRequest;
import org.eclipse.tractusx.sde.bpndiscovery.model.request.BpnDiscoverySearchRequest;
import org.eclipse.tractusx.sde.bpndiscovery.model.response.BpnDiscoveryBatchResponse;
import org.eclipse.tractusx.sde.bpndiscovery.model.response.BpnDiscoveryResponse;
import org.eclipse.tractusx.sde.bpndiscovery.model.response.BpnDiscoverySearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "IBpndiscoveryExternalServiceApi", url = "${bpndiscovery.hostname}", configuration = BpndiscoveryExternalServiceApi.class)
public interface IBpndiscoveryExternalServiceApi {

	@PostMapping(path = "/administration/connectors/bpnDiscovery")
	BpnDiscoveryResponse bpnDiscoveryDataByKey(@RequestBody BpnDiscoveryRequest bpnDiscoveryKey);

	@PostMapping(path = "/administration/connectors/bpnDiscovery/batch")
	List<BpnDiscoveryBatchResponse> bpnDiscoveryBatchDataByList(
			@RequestBody List<BpnDiscoveryRequest> bpnDiscoveryKeyList);

	@PostMapping(path = "/administration/connectors/bpnDiscovery/search")
	BpnDiscoverySearchResponse bpnDiscoverySearchData(@RequestBody BpnDiscoverySearchRequest bpnDiscoverySearchRequest);

	@DeleteMapping(path = "/administration/connectors/bpnDiscovery/{resourceId}")
	ResponseEntity<Object> deleteBpnDiscoveryData(@PathVariable String resourceId);

}
