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

package org.eclipse.tractusx.sde.edc.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellLookupRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellDescriptorResponseList;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.ShellLookupResponse;
import org.eclipse.tractusx.sde.digitaltwins.entities.response.SubModelResponse;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsUtility;
import org.eclipse.tractusx.sde.digitaltwins.gateways.external.EDCDigitalTwinProxyForLookUp;
import org.eclipse.tractusx.sde.edc.model.edr.EDRCachedByIdResponse;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import feign.FeignException;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookUpDTTwin {

	private final EDCDigitalTwinProxyForLookUp eDCDigitalTwinProxyForLookUp;

	private final DigitalTwinsUtility digitalTwinsUtility;

	@Value(value = "${manufacturerId}")
	private String manufacturerId;

	@Value(value = "${digital-twins.managed.thirdparty:false}")
	private boolean managedThirdParty;

	@SneakyThrows
	public List<SubModelResponse> lookUpTwin(EDRCachedByIdResponse edrToken, QueryDataOfferModel dtOffer,
			String manufacturerPartId, String bpnNumber, String submodel) {

		String endpoint = edrToken.getEndpoint();
		String dtOfferUrl = dtOffer.getConnectorOfferUrl();
		Map<String, String> header = new HashMap<>();
		header.put(edrToken.getAuthKey(), edrToken.getAuthCode());
		submodel = StringUtils.isBlank(submodel) ? "" : submodel;

		if (StringUtils.isNotBlank(bpnNumber))
			header.put("Edc-Bpn", bpnNumber);
		if (StringUtils.isBlank(manufacturerPartId)) {
			return lookUpAllShellForBPN(submodel, endpoint, dtOfferUrl, header);
		} else {
			return lookUpTwinBasedOnBPNAndManufacturerPartId(manufacturerPartId, bpnNumber, submodel, endpoint,
					dtOfferUrl, header);
		}
	}

	private List<SubModelResponse> lookUpTwinBasedOnBPNAndManufacturerPartId(String manufacturerPartId,
			String bpnNumber, String submodel, String endpoint, String dtOfferUrl, Map<String, String> header) {
		ShellLookupRequest shellLookupRequest = getShellLookupRequest(manufacturerPartId, bpnNumber, submodel);
		try {

			String assetIds = managedThirdParty
					? digitalTwinsUtility.encodeAssetIdsObject(shellLookupRequest.toJsonString())
					: shellLookupRequest.toJsonString();

			ShellLookupResponse shellLookup = eDCDigitalTwinProxyForLookUp.shellLookup(new URI(endpoint), assetIds,
					header);

			return getSubmodelDetails(shellLookupRequest, endpoint, header, dtOfferUrl, shellLookup.getResult(),
					submodel);

		} catch (FeignException e) {
			String errorMsg = "Unable to lookUpTwinBasedOnManufacturerPartId " + dtOfferUrl + ", "
					+ shellLookupRequest.toJsonString() + " because: " + e.contentUTF8();
			log.error("FeignException : " + errorMsg);
		} catch (Exception e) {
			String errorMsg = "Unable to lookUpTwinBasedOnManufacturerPartId " + dtOfferUrl + ", "
					+ shellLookupRequest.toJsonString() + "because: " + e.getMessage();
			log.error("Exception : " + errorMsg);
		}
		return Collections.emptyList();
	}

	private List<SubModelResponse> lookUpAllShellForBPN(String submodel, String endpoint, String dtOfferUrl,
			Map<String, String> header) {

		List<SubModelResponse> ls = new ArrayList<>();
		try {
			ShellDescriptorResponseList allShell = eDCDigitalTwinProxyForLookUp.getAllShell(new URI(endpoint), 0, 10000,
					header);
			for (ShellDescriptorResponse shellDescriptorResponse : allShell.getResult())
				preapreSubmodelResult(submodel, ls, shellDescriptorResponse.getSubmodelDescriptors());

		} catch (FeignException e) {
			String errorMsg = "Unable to lookUpAllShellForBPN " + dtOfferUrl + ", " + endpoint + "," + " because: "
					+ e.contentUTF8();
			log.error("FeignException : " + errorMsg);
		} catch (Exception e) {
			String errorMsg = "Unable to lookUpAllShellForBPN " + dtOfferUrl + ", " + endpoint + "because: "
					+ e.getMessage();
			log.error("Exception : " + errorMsg);
		}
		return ls;
	}

	@SneakyThrows
	private List<SubModelResponse> getSubmodelDetails(ShellLookupRequest shellLookupRequest, String endpoint,
			Map<String, String> header, String dtOfferUrl, List<String> shellIds, String submodel) {
		List<SubModelResponse> ls = new ArrayList<>();

		for (String shellId : shellIds) {
			ShellDescriptorResponse shellDescriptorResponse = eDCDigitalTwinProxyForLookUp.getShellDescriptorByShellId(
					new URI(endpoint), digitalTwinsUtility.encodeShellIdBase64Utf8(shellId), header);
			preapreSubmodelResult(submodel, ls, shellDescriptorResponse.getSubmodelDescriptors());
		}
		return ls;
	}

	private void preapreSubmodelResult(String submodel, List<SubModelResponse> ls,
			List<SubModelResponse> shellDescriptorResponseList) {

		for (SubModelResponse subModelResponse : shellDescriptorResponseList) {
			if (!subModelResponse.getIdShort().isEmpty()
					&& subModelResponse.getIdShort().toLowerCase().contains(submodel.toLowerCase())) {
				ls.add(subModelResponse);
			}
		}
	}

	private ShellLookupRequest getShellLookupRequest(String manufacturerPartId, String bpnNumber, String submodel) {

		ShellLookupRequest shellLookupRequest = new ShellLookupRequest();
		getSpecificAssetIds(manufacturerPartId, bpnNumber, submodel).entrySet().stream()
				.forEach(entry -> shellLookupRequest.addLocalIdentifier(entry.getKey(), entry.getValue()));

		return shellLookupRequest;
	}

	private Map<String, String> getSpecificAssetIds(String manufacturerPartId, String bpnNumber, String submodel) {
		Map<String, String> specificIdentifiers = new HashMap<>();

		if (StringUtils.isNotBlank(manufacturerPartId))
			specificIdentifiers.put(CommonConstants.MANUFACTURER_PART_ID, manufacturerPartId);

		if (StringUtils.isNotBlank(bpnNumber))
			specificIdentifiers.put(CommonConstants.MANUFACTURER_ID, bpnNumber);

		if (StringUtils.isNotBlank(submodel) && "pcf".equals(submodel))
			specificIdentifiers.put(CommonConstants.ASSET_LIFECYCLE_PHASE, CommonConstants.AS_PLANNED);

		return specificIdentifiers;
	}
}
