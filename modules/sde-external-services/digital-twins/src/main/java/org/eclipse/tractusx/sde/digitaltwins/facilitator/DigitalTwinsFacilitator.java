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
import java.util.Base64;
import java.util.List;

import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.CreateSubModelRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellDescriptorRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellLookupRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellLookupResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelListResponse;
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

	private final DigitalTwinsFeignClient digitalTwinsFeignClient;

	@Value(value = "${digital-twins.hostname:default}")
	private String digitalTwinsHost;

	@Value(value = "${digital-twins.api:/api/v3.0}")
	private String dtApiUri;

	@Value(value = "${manufacturerId}")
	public String manufacturerId;

	public List<String> shellLookup(ShellLookupRequest request) throws ServiceException {
		return shellLookupFromDDTR(request, null, manufacturerId);
	}

	@SneakyThrows
	public List<String> shellLookupFromDDTR(ShellLookupRequest request, String ddtrUrl, String edcBPN)
			throws ServiceException {

		URI dtURL = (ddtrUrl == null || ddtrUrl.length() <= 0) ? getDtURL(digitalTwinsHost) : getDtURL(ddtrUrl);

		List<String> shellIds = List.of();

		try {
			ResponseEntity<ShellLookupResponse> response = digitalTwinsFeignClient.shellLookup(dtURL,
					request.toJsonString(), edcBPN);

			ShellLookupResponse body = response.getBody();
			if (response.getStatusCode() == HttpStatus.OK && body != null) {
				shellIds = body.getResult();
			}

		} catch (Exception e) {
			String error = "Error in lookup DT lookup:" + dtURL + ", " + request.toJsonString() + ", " + e.getMessage();
			log.error(error);
			throw new ServiceException(error);
		}
		return shellIds;
	}

	@SneakyThrows
	public String deleteShell(String shellId) {
		String deleteResponse = "";
		try {
			ResponseEntity<Void> response = digitalTwinsFeignClient.deleteShell(getDtURL(digitalTwinsHost),
					encodeShellIdBase64Utf8(shellId));

			if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
				deleteResponse = "Asset identifier" + shellId + "deleted successfully";
			}
		} catch (Exception e) {
			String error = "Error in deleteShell :" + digitalTwinsHost + ", " + shellId + "," + e.getMessage();
			log.error(error);
			throw new ServiceException(error);
		}
		return deleteResponse;
	}

	@SneakyThrows
	public List<ShellDescriptorResponse> getShellDescriptorsWithSubmodelDetails(List<String> shellIds, String ddtrUrl) {

		List<ShellDescriptorResponse> items = new ArrayList<>();
		for (String shellId : shellIds) {
			items.add(getShellDetailsById(shellId, ddtrUrl));
		}
		return items;
	}

	public ShellDescriptorResponse getShellDetailsById(String shellId, String ddtrUrl) {
		
		URI dtURL = (ddtrUrl == null || ddtrUrl.length() <= 0) ? getDtURL(digitalTwinsHost) : getDtURL(ddtrUrl);

		ResponseEntity<ShellDescriptorResponse> shellDescriptorResponse = digitalTwinsFeignClient
				.getShellDescriptorByShellId(dtURL, encodeShellIdBase64Utf8(shellId));
		return shellDescriptorResponse.getBody();
	}

	@SneakyThrows
	public void deleteSubmodelfromShellById(String shellId, String subModelId) {
		try {
			digitalTwinsFeignClient.deleteSubmodelfromShellById(getDtURL(digitalTwinsHost),
					encodeShellIdBase64Utf8(shellId), encodeShellIdBase64Utf8(subModelId));
		} catch (Exception e) {
			parseExceptionMessage(e);
		}
	}

	public ShellDescriptorResponse createShellDescriptor(ShellDescriptorRequest request) {
		ShellDescriptorResponse responseBody;
		ResponseEntity<ShellDescriptorResponse> registerSubmodel = digitalTwinsFeignClient
				.createShellDescriptor(getDtURL(digitalTwinsHost), request);
		if (registerSubmodel.getStatusCode() != HttpStatus.CREATED) {
			responseBody = null;
		} else {
			responseBody = registerSubmodel.getBody();
		}
		return responseBody;
	}

	public void updateShellSpecificAssetIdentifiers(String shellId, List<Object> specificAssetIds) {

		digitalTwinsFeignClient.deleteShellSpecificAttributes(getDtURL(digitalTwinsHost),
				encodeShellIdBase64Utf8(shellId), manufacturerId);

		ResponseEntity<List<Object>> registerSubmodel = digitalTwinsFeignClient.createShellSpecificAttributes(
				getDtURL(digitalTwinsHost), encodeShellIdBase64Utf8(shellId), manufacturerId, specificAssetIds);
		if (registerSubmodel.getStatusCode() != HttpStatus.CREATED) {
			log.error("Error in shell SpecificAssetIdentifiers deletion: " + registerSubmodel.toString());
		}
	}

	public void createSubModel(String shellId, CreateSubModelRequest request) {

		request.setDescription(List.of());

		ResponseEntity<String> response = digitalTwinsFeignClient.createSubModel(getDtURL(digitalTwinsHost),
				encodeShellIdBase64Utf8(shellId), request);
		if (response.getStatusCode() != HttpStatus.CREATED) {
			log.error("Unable to create submodel descriptor");
		}

	}

	public SubModelListResponse getSubModels(String shellId) {

		ResponseEntity<SubModelListResponse> response = digitalTwinsFeignClient.getSubModels(getDtURL(digitalTwinsHost),
				encodeShellIdBase64Utf8(shellId));
		SubModelListResponse responseBody = null;
		if (response.getStatusCode() == HttpStatus.OK) {
			responseBody = response.getBody();
		}
		return responseBody;
	}

	private String encodeShellIdBase64Utf8(String shellId) {
		return Base64.getUrlEncoder().encodeToString(shellId.getBytes());
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
