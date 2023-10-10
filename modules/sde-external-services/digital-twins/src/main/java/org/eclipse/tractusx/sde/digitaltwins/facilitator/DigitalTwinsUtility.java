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

import static org.eclipse.tractusx.sde.common.constants.CommonConstants.ASSET_LIFECYCLE_PHASE;
import static org.eclipse.tractusx.sde.common.constants.CommonConstants.MANUFACTURER_PART_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.common.constants.CommonConstants;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Getter;
import lombok.SneakyThrows;

@Component
@Getter
public class DigitalTwinsUtility {

	private static final String PUBLIC_READABLE = "PUBLIC_READABLE";

	@Value(value = "${manufacturerId}")
	public String manufacturerId;

	@Value(value = "${edc.hostname}")
	public String edcEndpoint;

	ObjectMapper mapper = new ObjectMapper();

	private static final Map<String, List<String>> publicReadableSpecificAssetIDs = Map.of(MANUFACTURER_PART_ID,
			List.of("*"), ASSET_LIFECYCLE_PHASE, List.of("AsBuilt", "AsPlanned"));

	@SneakyThrows
	public ShellDescriptorRequest getShellDescriptorRequest(Map<String, String> specificIdentifiers, Object object) {

		JsonNode jsonNode = mapper.convertValue(object, ObjectNode.class);

		List<String> bpns = getFieldFromJsonNodeArray(jsonNode, "bpn_numbers");

		return ShellDescriptorRequest.builder()
				.idShort(String.format("%s_%s_%s", getFieldFromJsonNode(jsonNode, "name_at_manufacturer"),
						manufacturerId, getFieldFromJsonNode(jsonNode, "manufacturer_part_id")))
				.globalAssetId(getFieldFromJsonNode(jsonNode, "uuid"))
				.specificAssetIds(getSpecificAssetIds(specificIdentifiers, bpns)).description(List.of())
				.id(UUIdGenerator.getUrnUuid()).build();
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
				.protocolInformation(ProtocolInformation.builder()
						.endpointAddress(edcEndpoint + CommonConstants.SUBMODEL_CONTEXT_URL)
						.endpointProtocol(CommonConstants.HTTP)
						.endpointProtocolVersion(List.of(CommonConstants.ENDPOINT_PROTOCOL_VERSION))
						.subProtocol(CommonConstants.SUB_PROTOCOL)
						.subprotocolBody(encodedUrl("id=" + shellId + "-" + submodelIdentification) + ";dspEndpoint="
								+ edcEndpoint)
						.subprotocolBodyEncoding(CommonConstants.BODY_ENCODING)
						.securityAttributes(List.of(new SecurityAttributes("NONE", "NONE", "NONE"))).build())
				.build());
		return endpoints;
	}

	@SneakyThrows
	public List<Object> getSpecificAssetIds(Map<String, String> specificAssetIds, List<String> bpns) {

		List<Object> specificIdentifiers = new ArrayList<>();
		
		List<Keys> keyList = bpnKeyRefrence(bpns);
		
		specificAssetIds.entrySet().stream().forEach(entry -> {

			List<String> list = publicReadableSpecificAssetIDs.get(entry.getKey());
			ExternalSubjectId externalSubjectId = null;

			if (list != null && (list.contains("*") || list.contains(entry.getValue()))) {
				externalSubjectId = ExternalSubjectId.builder()
						.type("ExternalReference")
						.keys(List.of(Keys.builder().type("GlobalReference").value(PUBLIC_READABLE).build()))
						.build();
				specificIdentifiers.add(new KeyValuePair(entry.getKey(), entry.getValue(), externalSubjectId));
			}
			else {
				if (keyList != null && !keyList.isEmpty() && !entry.getValue().isEmpty()) {
					
					externalSubjectId = ExternalSubjectId.builder()
							.type("ExternalReference").keys(keyList)
							.build();
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
			return bpns.stream()
					.map(bpn -> Keys.builder().type("GlobalReference").value(bpn).build())
					.toList();
		}
		return Collections.emptyList();
	}

	private String encodedUrl(String format) {
		return format.replace(":", "%3A");
	}

	private String getFieldFromJsonNode(JsonNode jnode, String fieldName) {
		if (jnode.get(fieldName) != null)
			return jnode.get(fieldName).asText();
		else
			return "";
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

}
