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

package org.eclipse.tractusx.sde.digitaltwins.facilitator;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.CreateSubModelRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellDescriptorRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellLookupRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellLookupResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelListResponse;
import org.eclipse.tractusx.sde.digitaltwins.gateways.external.AuthTokenUtility;
import org.eclipse.tractusx.sde.digitaltwins.gateways.external.DigitalTwinsFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DigitalTwinsFacilitator {

	private static final String AUTHORIZATION = "Authorization";

	private final DigitalTwinsFeignClient digitalTwinsFeignClient;
	private final AuthTokenUtility authTokenUtility;

	@Value(value = "${digital-twins.hostname:default}")
	private String digitalTwinsHost;

	@Value(value = "${digital-twins.api:/api/v3.0}")
	private String dtApiUri;

	public List<String> shellLookup(ShellLookupRequest request) throws ServiceException {
		return shellLookupFromDDTR(request, null);
	}

	@SneakyThrows
	public List<String> shellLookupFromDDTR(ShellLookupRequest request, String ddtrUrl) throws ServiceException {

		URI dtURL = (ddtrUrl == null || ddtrUrl.length() <= 0) ? getDtURL(digitalTwinsHost) : getDtURL(ddtrUrl);

		List<String> shellIds = List.of();

		try {
			ResponseEntity<ShellLookupResponse> response = digitalTwinsFeignClient.shellLookup(dtURL,
					request.toJsonString(), getHeaders());

			if (response.getStatusCode() != HttpStatus.OK) {
			} else {
				shellIds = response.getBody().getResult();
			}
		} catch (Exception e) {
			String error = "Error in lookup DT lookup:" + ddtrUrl + ", " + request.toJsonString() + ", "
					+ e.getMessage();
			log.error(error);
			throw new ServiceException(error);
		}
		return shellIds;
	}

	@SneakyThrows
	public String deleteShell(String assetId) {
		String deleteResponse = "";
		try {
			ResponseEntity<Void> response = digitalTwinsFeignClient.deleteShell(getDtURL(digitalTwinsHost), assetId,
					getHeaders());

			if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
				deleteResponse = "Asset identifier" + assetId + "deleted successfully";
			}
		} catch (Exception e) {
			String error = "Error in deleteShell :" + digitalTwinsHost + ", " + assetId + "," + e.getMessage();
			log.error(error);
			throw new ServiceException(error);
		}
		return deleteResponse;
	}

	@SneakyThrows
	public List<ShellDescriptorResponse> getShellDescriptorsWithSubmodelDetails(List<String> shellIds) {

		List<ShellDescriptorResponse> items = new ArrayList<>();
		for (String shellId : shellIds) {
			items.add(getShellDetailsById(shellId));
		}
		return items;
	}

	public ShellDescriptorResponse getShellDetailsById(String shellId) {
		ResponseEntity<ShellDescriptorResponse> shellDescriptorResponse = digitalTwinsFeignClient
				.getShellDescriptorByShellId(getDtURL(digitalTwinsHost), getHeaders(), shellId);
		return shellDescriptorResponse.getBody();
	}

	@SneakyThrows
	public void deleteSubmodelfromShellById(String shellId, String subModelId) {
		try {
			digitalTwinsFeignClient.deleteSubmodelfromShellById(getDtURL(digitalTwinsHost), shellId, subModelId,
					getHeaders());
		} catch (Exception e) {
			parseExceptionMessage(e);
		}
	}

	private Map<String, String> getHeaders() {
		Map<String, String> headers = new HashMap<>();
		headers.put(AUTHORIZATION, authTokenUtility.getToken());
		return headers;
	}

	public ShellDescriptorResponse createShellDescriptor(ShellDescriptorRequest request) {
		ShellDescriptorResponse responseBody;
		ResponseEntity<ShellDescriptorResponse> registerSubmodel = digitalTwinsFeignClient
				.createShellDescriptor(getDtURL(digitalTwinsHost), getHeaders(), request);
		if (registerSubmodel.getStatusCode() != HttpStatus.CREATED) {
			responseBody = null;
		} else {
			responseBody = registerSubmodel.getBody();
		}
		return responseBody;
	}

	public void createSubModel(String shellId, CreateSubModelRequest request) {

		ResponseEntity<String> response = digitalTwinsFeignClient.createSubModel(getDtURL(digitalTwinsHost), shellId,
				getHeaders(), request);
		if (response.getStatusCode() != HttpStatus.CREATED) {
			log.error("Unable to create submodel descriptor");
		}

	}

	public SubModelListResponse getSubModels(String shellId) {

		ResponseEntity<SubModelListResponse> response = digitalTwinsFeignClient.getSubModels(getDtURL(digitalTwinsHost),
				shellId, getHeaders());
		SubModelListResponse responseBody = null;
		if (response.getStatusCode() == HttpStatus.OK) {
			responseBody = response.getBody();
		}
		return responseBody;
	}

	public void parseExceptionMessage(Exception e) throws ServiceException {

		if (!e.toString().contains("FeignException$NotFound") || !e.toString().contains("404 Not Found")) {
			throw new ServiceException("Exception in Digital delete request process: " + e.getMessage());
		}
	}

	@SneakyThrows
	private URI getDtURL(String dtURL) {
		return new URI(dtURL.concat(dtApiUri));
	}
}
