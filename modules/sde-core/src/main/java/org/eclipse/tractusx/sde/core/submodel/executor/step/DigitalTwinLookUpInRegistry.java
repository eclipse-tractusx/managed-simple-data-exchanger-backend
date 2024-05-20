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

package org.eclipse.tractusx.sde.core.submodel.executor.step;

import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerDigitalTwinUseCaseException;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellLookupRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellLookupResponse;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsFacilitator;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsUtility;
import org.eclipse.tractusx.sde.digitaltwins.gateways.external.EDCDigitalTwinProxyForLookUp;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedByIdResponse;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.edc.util.EDCAssetUrlCacheService;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DigitalTwinLookUpInRegistry {

	private final DigitalTwinsUtility digitalTwinsUtility;

	private final EDCAssetUrlCacheService edcAssetUrlCacheService;

	private final EDCDigitalTwinProxyForLookUp eDCDigitalTwinProxyForLookUp;

	private final DigitalTwinsFacilitator digitalTwinFacilitator;
	
	private ObjectMapper mapper= new ObjectMapper();

	@SneakyThrows
	public String lookupTwinInLocalRrgistry(Integer rowIndex, Map<String, String> specificAssetIds,
			ObjectNode jsonObject, JsonObject asJsonObject) {

		ShellLookupRequest shellLookupRequest = digitalTwinsUtility.getShellLookupRequest(specificAssetIds);
		List<String> shellIds = digitalTwinFacilitator.shellLookup(shellLookupRequest);
		
		String shellId;

		if (shellIds.isEmpty()) {
			throw new CsvHandlerDigitalTwinUseCaseException(
					String.format("No relational aspect found in DT %s", shellLookupRequest.toJsonString()));
		} else if (shellIds.size() == 1) {
			log.debug(String.format("Shell id found for '%s'", shellLookupRequest.toJsonString()));
			shellId = shellIds.stream().findFirst().orElse(null);
			log.debug(String.format("Shell id '%s'", shellId));
		} else {
			throw new CsvHandlerDigitalTwinUseCaseException(
					String.format("Multiple ids found on aspect %s", shellLookupRequest.toJsonString()));
		}

		List<ShellDescriptorResponse> shellDescriptorResponseList = digitalTwinFacilitator
				.getShellDescriptorsWithSubmodelDetails(shellIds);
		String shellGlobalassetId = null;
		
		for (ShellDescriptorResponse shellDescriptorResponse : shellDescriptorResponseList) {
			shellGlobalassetId = shellDescriptorResponse.getGlobalAssetId();
		}
		
		return shellGlobalassetId;
	}

	@SneakyThrows
	public String lookupTwinRemotely(Integer rowIndex, Map<String, String> specificAssetIds,
			String bpnForRemoteRegistry, ObjectNode jsonObject, JsonObject asJsonObject) {

		ShellLookupRequest shellLookupRequest = digitalTwinsUtility.getShellLookupRequest(specificAssetIds);

		List<QueryDataOfferModel> queryDataOffers = edcAssetUrlCacheService.getDDTRUrl(bpnForRemoteRegistry);

		String childUUID = null;
		String msg = "";

		for (QueryDataOfferModel dtOffer : queryDataOffers) {

			EDRCachedByIdResponse edrToken = edcAssetUrlCacheService.verifyAndGetToken(bpnForRemoteRegistry, dtOffer);

			if (edrToken != null) {
				childUUID = lookUpChildTwin(rowIndex, shellLookupRequest, jsonObject, bpnForRemoteRegistry, edrToken,
						dtOffer);
				if (childUUID != null) {
					break;
				}
			} else {
				msg = ", EDC connector " + dtOffer.getConnectorOfferUrl() + ", assetId " + dtOffer.getAssetId()
						+ ", The EDR token is null to find child twin ";
				log.warn(rowIndex + msg + shellLookupRequest.toJsonString());
			}
		}

		if (queryDataOffers.isEmpty()) {
			throw new CsvHandlerUseCaseException(rowIndex, "No DTR registry found for child aspect look up");
		}

		if (childUUID == null) {
			throw new CsvHandlerUseCaseException(rowIndex,
					"No child aspect found for " + shellLookupRequest.toJsonString());
		}
		
		return childUUID;
	}

	@SneakyThrows
	private String lookUpChildTwin(Integer rowIndex, ShellLookupRequest shellLookupRequest, ObjectNode jsonObject,
			String bpnForRemoteRegistry, EDRCachedByIdResponse edrToken, QueryDataOfferModel dtOffer) {

		String childUUID = null;
		String endpoint = edrToken.getEndpoint();
		String dtOfferUrl = dtOffer.getConnectorOfferUrl();
		try {

			Map<String, String> header = new HashMap<>();
			header.put("authorization", edrToken.getAuthorization());
			header.put("Edc-Bpn", bpnForRemoteRegistry);

			String shellLookup = eDCDigitalTwinProxyForLookUp.shellLookup(new URI(endpoint),
					digitalTwinsUtility.encodeAssetIdsObjectOnlyPartInstanceId(shellLookupRequest), header);
			
			ShellLookupResponse response = mapper.readValue(shellLookup, ShellLookupResponse.class);

			childUUID = getChildSubmodelDetails(rowIndex, shellLookupRequest, endpoint, header, jsonObject, dtOfferUrl,
					response.getResult());

		} catch (FeignException e) {
			String err = e.contentUTF8();
			err = StringUtils.isBlank(err) ? e.getMessage() : err;
			String errorMsg = "Unable to look up child twin " + dtOfferUrl + ", assetId " + dtOffer.getAssetId() + ", "
					+ shellLookupRequest.toJsonString() + " because: " + err;
			log.error("FeignException : " + errorMsg);
		} catch (CsvHandlerDigitalTwinUseCaseException e) {
			throw e;
		} catch (Exception e) {
			String errorMsg = "Unable to look up child twin " + dtOfferUrl + ", assetId " + dtOffer.getAssetId() + ", "
					+ shellLookupRequest.toJsonString() + "because: " + e.getMessage();
			log.error("Exception : " + errorMsg);
		}

		return childUUID;
	}

	@SneakyThrows
	private String getChildSubmodelDetails(Integer rowIndex, ShellLookupRequest shellLookupRequest, String endpoint,
			Map<String, String> header, ObjectNode jsonObject, String dtOfferUrl, List<String> childshellIds)
			throws CsvHandlerDigitalTwinUseCaseException {

		String childUUID = null;
		if (childshellIds == null) {
			log.warn(rowIndex + ", " + dtOfferUrl + ", No child aspect found for " + shellLookupRequest.toJsonString());
		} else if (childshellIds.size() > 1) {
			throw new CsvHandlerDigitalTwinUseCaseException(String.format(
					"Multiple shell id's found for childAspect %s, %s", dtOfferUrl, shellLookupRequest.toJsonString()));
		} else if (childshellIds.size() == 1) {

			String shellDescriptorResponseStr = eDCDigitalTwinProxyForLookUp.getShellDescriptorByShellId(
					new URI(endpoint), encodeShellIdBase64Utf8(childshellIds.get(0)), header);
			
			ShellDescriptorResponse shellDescriptorResponse = mapper.readValue(shellDescriptorResponseStr,
					ShellDescriptorResponse.class); 
			
			childUUID = shellDescriptorResponse.getGlobalAssetId();

			log.info(rowIndex + ", " + dtOfferUrl + ", Child aspect found for " + shellLookupRequest.toJsonString());
		}

		return childUUID;
	}

	private String encodeShellIdBase64Utf8(String shellId) {
		return Base64.getUrlEncoder().encodeToString(shellId.getBytes());
	}

}