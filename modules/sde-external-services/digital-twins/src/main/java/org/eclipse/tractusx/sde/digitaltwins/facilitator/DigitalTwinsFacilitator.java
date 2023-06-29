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
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubmodelDescriptionListResponse;
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
	private URI digitalTwinsHost;

	public ShellLookupResponse shellLookup(ShellLookupRequest request) throws ServiceException {
		return shellLookupFromDDTR(request, null);
	}

	@SneakyThrows
	public ShellLookupResponse shellLookupFromDDTR(ShellLookupRequest request, String ddtrUrl) throws ServiceException {

		URI dtURL = (ddtrUrl == null || ddtrUrl.length() <= 0) ? digitalTwinsHost : new URI(ddtrUrl);

		ShellLookupResponse responseBody = null;

		try {
			ResponseEntity<ShellLookupResponse> response = digitalTwinsFeignClient.shellLookup(dtURL,
					request.toJsonString(), getHeaders());

			if (response.getStatusCode() != HttpStatus.OK) {
				responseBody = new ShellLookupResponse();
			} else {
				responseBody = response.getBody();
			}
		} catch (Exception e) {
			String error = "Error in lookup DT lookup:" + ddtrUrl + ", " + request.toJsonString() + ", "
					+ e.getMessage();
			log.error(error);
			throw new ServiceException(error);
		}
		return responseBody;
	}

	@SneakyThrows
	public String deleteShell(String assetId) {
		String deleteResponse = "";
		try {
			ResponseEntity<Void> response = digitalTwinsFeignClient.deleteShell(digitalTwinsHost, assetId,
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
	public SubmodelDescriptionListResponse getShellDescriptorsWithSubmodelDetails(List<String> shellIds) {
		return digitalTwinsFeignClient.getShellDescriptorsWithSubmodelDetails(digitalTwinsHost, getHeaders(), shellIds);
	}

	@SneakyThrows
	public void deleteSubmodelfromShellById(String shellId, String subModelId) {
		try {
			digitalTwinsFeignClient.deleteSubmodelfromShellById(digitalTwinsHost, shellId, subModelId, getHeaders());
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
				.createShellDescriptor(digitalTwinsHost, getHeaders(), request);
		if (registerSubmodel.getStatusCode() != HttpStatus.CREATED) {
			responseBody = null;
		} else {
			responseBody = registerSubmodel.getBody();
		}
		return responseBody;
	}

	public void createSubModel(String shellId, CreateSubModelRequest request) {

		ResponseEntity<String> response = digitalTwinsFeignClient.createSubModel(digitalTwinsHost, shellId,
				getHeaders(), request);
		if (response.getStatusCode() != HttpStatus.CREATED) {
			log.error("Unable to create submodel descriptor");
		}

	}

	public SubModelListResponse getSubModels(String shellId) {

		ResponseEntity<SubModelListResponse> response = digitalTwinsFeignClient.getSubModels(digitalTwinsHost, shellId,
				getHeaders());
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
}
