/********************************************************************************
 * Copyright (c) 2022, 2024 T-Systems International GmbH
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

import static org.eclipse.tractusx.sde.common.constants.CommonConstants.ASSET_LIFECYCLE_PHASE;
import static org.eclipse.tractusx.sde.common.constants.CommonConstants.MANUFACTURER_PART_ID;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.utils.PolicyOperationUtil;
import org.eclipse.tractusx.sde.common.utils.UUIdGenerator;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.Endpoint;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.ExternalSubjectId;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.KeyValuePair;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.Keys;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.ProtocolInformation;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.SecurityAttributes;
import org.eclipse.tractusx.sde.digitaltwins.entities.common.SemanticId;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.CreateSubModelRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellDescriptorRequest;
import org.eclipse.tractusx.sde.digitaltwins.entities.request.ShellLookupRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.SneakyThrows;

@Component
@Getter
public class DigitalTwinsUtility {

	private static final String PUBLIC_READABLE = "PUBLIC_READABLE";

	@Value(value = "${manufacturerId}")
	public String manufacturerId;

	@Value(value = "${edc.hostname}${edc.dsp.endpointpath:/api/v1/dsp}")
	public String digitalTwinEdcDspEndpoint;

	@Value(value = "${edc.hostname}${edc.dataplane.endpointpath:/api/public}")
	public String digitalTwinEdcDataplaneEndpoint;

	ObjectMapper mapper = new ObjectMapper();

	private static final Map<String, List<String>> publicReadableSpecificAssetIDs = Map.of(MANUFACTURER_PART_ID,
			List.of("*"), ASSET_LIFECYCLE_PHASE, List.of("AsBuilt", "AsPlanned"));

	@SneakyThrows
	public ShellDescriptorRequest getShellDescriptorRequest(String nameAtManufacturer, String manufacturerPartId,
			String uuid, Map<String, String> specificIdentifiers, PolicyModel policy) {

		return ShellDescriptorRequest.builder()
				.idShort(String.format("%s_%s_%s", nameAtManufacturer, manufacturerId, manufacturerPartId))
				.globalAssetId(uuid)
				.specificAssetIds(getSpecificAssetIds(specificIdentifiers, policy))
				.description(List.of())
				.id(UUIdGenerator.getUrnUuid())
				.build();
	}

	@SneakyThrows
	public CreateSubModelRequest getCreateSubModelRequest(String shellId, String sematicId, String idShortofModel) {
		String identification = UUIdGenerator.getUrnUuid();

		SemanticId semanticId = SemanticId.builder().type(CommonConstants.EXTERNAL_REFERENCE)
				.keys(List.of(new Keys(CommonConstants.GLOBAL_REFERENCE, sematicId))).build();

		List<Endpoint> endpoints = prepareDtEndpoint(shellId, identification);

		return CreateSubModelRequest.builder().id(identification).idShort(idShortofModel).semanticId(semanticId)
				.endpoints(endpoints).build();
	}

	@SneakyThrows
	public CreateSubModelRequest getCreateSubModelRequestForChild(String shellId, String sematicId,
			String idShortofModel, String identification) {

		SemanticId semanticId = SemanticId.builder().type(CommonConstants.EXTERNAL_REFERENCE)
				.keys(List.of(new Keys(CommonConstants.GLOBAL_REFERENCE, sematicId))).build();

		List<Endpoint> endpoints = prepareDtEndpoint(shellId, identification);

		return CreateSubModelRequest.builder().idShort(idShortofModel).id(identification).semanticId(semanticId)
				.endpoints(endpoints).build();
	}

	public List<Endpoint> prepareDtEndpoint(String shellId, String submodelIdentification) {
		List<Endpoint> endpoints = new ArrayList<>();
		endpoints.add(Endpoint.builder().endpointInterface(CommonConstants.INTERFACE)
				.protocolInformation(ProtocolInformation.builder().endpointAddress(digitalTwinEdcDataplaneEndpoint)
						.endpointProtocol(CommonConstants.HTTP)
						.endpointProtocolVersion(List.of(CommonConstants.ENDPOINT_PROTOCOL_VERSION))
						.subProtocol(CommonConstants.SUB_PROTOCOL)
						.subprotocolBody("id=" + shellId + "-" + submodelIdentification + ";dspEndpoint="
								+ digitalTwinEdcDspEndpoint)
						.subprotocolBodyEncoding(CommonConstants.BODY_ENCODING)
						.securityAttributes(List.of(new SecurityAttributes("NONE", "NONE", "NONE"))).build())
				.build());
		return endpoints;
	}

	@SneakyThrows
	public List<Object> getSpecificAssetIds(Map<String, String> specificAssetIds, PolicyModel policy) {

		List<Object> specificIdentifiers = new ArrayList<>();

		List<Keys> keyList = bpnKeyRefrence(PolicyOperationUtil.getAccessBPNList(policy));

		specificAssetIds.entrySet().stream().forEach(entry -> {

			List<String> list = publicReadableSpecificAssetIDs.get(entry.getKey());
			ExternalSubjectId externalSubjectId = null;

			if (list != null && (list.contains("*") || list.contains(entry.getValue()))) {
				externalSubjectId = ExternalSubjectId.builder().type("ExternalReference")
						.keys(List.of(Keys.builder().type("GlobalReference").value(PUBLIC_READABLE).build())).build();
				specificIdentifiers.add(new KeyValuePair(entry.getKey(), entry.getValue(), externalSubjectId));
			} else {
				if (keyList != null && !keyList.isEmpty() && !entry.getValue().isEmpty()) {

					externalSubjectId = ExternalSubjectId.builder().type("ExternalReference").keys(keyList).build();
					specificIdentifiers.add(new KeyValuePair(entry.getKey(), entry.getValue(), externalSubjectId));

				} else {
					Map<String, Object> map = new HashMap<>();
					map.put("name", entry.getKey());
					map.put("value", entry.getValue());
					specificIdentifiers.add(map);
				}
			}
		});

		return specificIdentifiers;
	}

	private List<Keys> bpnKeyRefrence(List<String> bpns) {
		if (bpns != null && !(bpns.size() == 1 && bpns.contains(manufacturerId))) {
			return bpns.stream().map(bpn -> Keys.builder().type("GlobalReference").value(bpn).build()).toList();
		}
		return Collections.emptyList();
	}


	@SneakyThrows
	private List<String> getFieldFromJsonNodeArray(JsonNode jsonNode, String fieldName) {
		ObjectMapper objectMapper = new ObjectMapper();

		if (jsonNode.get(fieldName) != null)
			return objectMapper.readValue(jsonNode.get(fieldName).toString(), new TypeReference<List<String>>() {
			});

		else
			return List.of();
	}

	public ShellLookupRequest getShellLookupRequest(Map<String, String> specificAssetIds) {

		ShellLookupRequest shellLookupRequest = new ShellLookupRequest();
		specificAssetIds.entrySet().stream()
				.forEach(entry -> shellLookupRequest.addLocalIdentifier(entry.getKey(), entry.getValue()));

		return shellLookupRequest;
	}

	public String encodeAssetIdsObject(String assetIdsList) {

		return encodeShellIdBase64Utf8(assetIdsList.replace("[", "").replace("]", ""));
	}

	public String encodeShellIdBase64Utf8(String shellId) {
		return Base64.getUrlEncoder().encodeToString(shellId.getBytes());
	}

}