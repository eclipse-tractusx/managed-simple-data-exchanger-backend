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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.configuration.properties.SDEConfigurationProperties;
import org.eclipse.tractusx.sde.common.constants.SubmoduleCommonColumnsConstant;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.submodel.executor.DatabaseUsecaseStep;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.common.utils.PolicyOperationUtil;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsFacilitator;
import org.eclipse.tractusx.sde.digitaltwins.facilitator.DigitalTwinsUtility;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DigitalTwinAccessRuleFacilator extends Step {

	private final DigitalTwinsFacilitator digitalTwinFacilitator;

	private final DigitalTwinsUtility digitalTwinsUtility;

	private final SDEConfigurationProperties sdeConfigProperties;

	@Qualifier("DatabaseUsecaseHandler")
	private final DatabaseUsecaseStep databaseUseCaseStep;

	private static final String PUBLIC_READABLE = "PUBLIC_READABLE";

	private String createRuleTemplate = """
			{
			    "validFrom": "2020-01-02T03:04:05Z",
			    "validTo": "4999-01-02T03:04:05Z",
			    "description": "ACME policy within set validity period",
			    "policyType": "AAS",
			    "policy": {
			        "accessRules": [
			            {
			                "attribute": "bpn",
			                "operator": "eq",
			                "value": "%s"
			            },
			            {
			                "attribute": "mandatorySpecificAssetIds",
			                "operator": "includes",
			                "values": [ %s ]
			            },
			            {
			                "attribute": "visibleSpecificAssetIdNames",
			                "operator": "includes",
			                "values": [ %s ]
			            },
			            {
			                "attribute": "visibleSemanticIds",
			                "operator": "includes",
			                "values": [
			                    {
			                        "attribute": "modelUrn",
			                        "operator": "eq",
			                        "value": "%s"
			                    }
			                ]
			            }
			        ]
			    }
			}
			""";

	@SneakyThrows
	public void createAccessRule(Integer rowIndex, ObjectNode jsonObject, Map<String, String> specificAssetIds,
			PolicyModel policy, String sematicId) {
		try {

			deleteAllExistingDTAccessRulesIfExist(rowIndex, jsonObject);

			List<String> accessBPNList = PolicyOperationUtil.getAccessBPNList(policy);
			List<String> accessruleIds = new ArrayList<>();

			if (accessBPNList.isEmpty()) {
				createAccessRuleMethod(specificAssetIds, sematicId, accessruleIds, PUBLIC_READABLE);
			} else {
				for (String bpn : accessBPNList) {
					createAccessRuleMethod(specificAssetIds, sematicId, accessruleIds, bpn);
				}
			}

			jsonObject.put(SubmoduleCommonColumnsConstant.SHELL_ACCESS_RULE_IDS, StringUtils.join(accessruleIds, ","));

		} catch (Exception e) {
			log.error(String.format("Row: %s: DigitalTwin exception: unable to create digital twin access rule %s",
					rowIndex, specificAssetIds.toString()) + ", because: " + e.getMessage());
			throw new ServiceException(
					String.format("Row: %s: DigitalTwin exception: unable to create digital twin access rule %s",
							rowIndex, specificAssetIds.toString()));
		}
	}

	@SneakyThrows
	private void createAccessRuleMethod(Map<String, String> specificAssetIds, String sematicId,
			List<String> accessruleIds, String bpn) {

		String requestTemplate = String.format(createRuleTemplate, bpn,
				digitalTwinsUtility.createAccessRuleMandatorySpecificAssetIds(specificAssetIds),
				digitalTwinsUtility.createAccessRuleVisibleSpecificAssetIdNames(specificAssetIds), sematicId);

		JsonNode requestBody = new ObjectMapper().readTree(requestTemplate);

		JsonNode response = digitalTwinFacilitator.createAccessControlsRule(sdeConfigProperties.getManufacturerId(),
				requestBody);
		
		accessruleIds.add(response.get("id").asText());

	}

	private void deleteAllExistingDTAccessRulesIfExist(Integer rowIndex, ObjectNode jsonObject) {
		
		String identifier = getIdentifier(jsonObject, getIdentifierOfModel());
		JsonObject datinRow = null;
		try {
			databaseUseCaseStep.init(getSubmodelSchema());
			datinRow = databaseUseCaseStep.readCreatedTwinsDetails(identifier);

			if (datinRow != null && !datinRow.get(SubmoduleCommonColumnsConstant.SHELL_ACCESS_RULE_IDS).isJsonNull()) {

				String accessRules = datinRow.get(SubmoduleCommonColumnsConstant.SHELL_ACCESS_RULE_IDS).getAsString();

				Arrays.asList(accessRules.split(",")).stream()
				.filter(StringUtils::isNotBlank)
				.forEach(str -> {
					try {
						str = str.trim();
						digitalTwinFacilitator.deleteAccessControlsRule(accessRules,
								sdeConfigProperties.getManufacturerId());
					} catch (Exception e) {
						log.error(String.format(
								"Row: %s: DigitalTwin: unable to delete existing DT access rule identifier %s, access rule id %s", rowIndex,
								identifier, str));
					}
				});
			}
		} catch (NoDataFoundException e) {
			log.debug(String.format("Row: %s: DigitalTwin: no existing digital twin access rule data found %s", rowIndex,
					identifier));
		}
	}
}