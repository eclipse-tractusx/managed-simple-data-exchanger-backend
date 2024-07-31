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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tractusx.sde.common.entities.Policies;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.eclipse.tractusx.sde.edc.api.ContractOfferCatalogApi;
import org.eclipse.tractusx.sde.edc.constants.EDCAssetConstant;
import org.eclipse.tractusx.sde.edc.facilitator.AbstractEDCStepsHelper;
import org.eclipse.tractusx.sde.edc.model.contractoffers.ContractOfferRequestFactory;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.edc.util.UtilityFunctions;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CatalogResponseBuilder extends AbstractEDCStepsHelper {

	private final ContractOfferCatalogApi contractOfferCatalogApiProxy;
	private final ContractOfferRequestFactory contractOfferRequestFactory;

	public List<QueryDataOfferModel> queryOnDataOffers(String providerUrl, String counterPartyId, Integer offset, Integer limit,
			String filterExpression) {

		providerUrl = UtilityFunctions.removeLastSlashOfUrl(providerUrl);

		if (!providerUrl.endsWith(protocolPath) && appendProtocolPath)
			providerUrl = providerUrl + protocolPath;

		String sproviderUrl = providerUrl;

		List<QueryDataOfferModel> queryOfferResponse = new ArrayList<>();

		JsonNode contractOfferCatalog = contractOfferCatalogApiProxy.getContractOffersCatalog(
				contractOfferRequestFactory.getContractOfferRequest(sproviderUrl, counterPartyId, limit, offset, filterExpression));

		JsonNode jOffer = contractOfferCatalog.get("dcat:dataset");
		if (jOffer.isArray()) {
			jOffer.forEach(
					offer -> handleContractOffer(sproviderUrl, contractOfferCatalog, offer, queryOfferResponse));
		} else {
			handleContractOffer(sproviderUrl, contractOfferCatalog, jOffer, queryOfferResponse);
		}

		return queryOfferResponse;
	}

	public void handleContractOffer(String sproviderUrl, JsonNode contractOfferCatalog, JsonNode offer, List<QueryDataOfferModel> queryOfferResponse) {

		JsonNode contractOffers = offer.get("odrl:hasPolicy");
		
		String edcstr = EDCAssetConstant.ASSET_PREFIX;
		
		QueryDataOfferModel build = QueryDataOfferModel.builder()
				.assetId(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_ID))
				.connectorOfferUrl(sproviderUrl)
				.title(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_NAME))
				.type(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_TYPE))
				.description(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_DESCRIPTION))
				.created(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_CREATED))
				.modified(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_MODIFIED))
				.publisher(getFieldFromJsonNode(contractOfferCatalog, edcstr + "participantId"))
				.version(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_VERSION))
				.fileName(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_FILENAME))
				.fileContentType(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_CONTENTTYPE))
				.connectorId(getFieldFromJsonNode(contractOfferCatalog, edcstr+"participantId"))
				.build();
		
		if (contractOffers.isArray()) {
			contractOffers.forEach(
					contractOffer -> queryOfferResponse.add(buildContractOffer(build, contractOffer)));
		} else {
			queryOfferResponse.add(buildContractOffer(build, contractOffers));
		}

	}

	public QueryDataOfferModel buildContractOffer(QueryDataOfferModel build, JsonNode contractOffer) {
		
		build.setOfferId(getFieldFromJsonNode(contractOffer, "@id"));
		build.setHasPolicy(contractOffer);
		checkAndSetPolicyPermission(build, contractOffer);
		return build;
	}
	
	private void checkAndSetPolicyPermission(QueryDataOfferModel build, JsonNode policy) {

		if (policy != null && policy.isArray()) {
			policy.forEach(pol -> {
				JsonNode permission = pol.get("odrl:permission");
				checkAndSetPolicyPermissionsConstraints(build, permission);
			});
		} else if (policy != null) {
			JsonNode permission = policy.get("odrl:permission");
			checkAndSetPolicyPermissionsConstraints(build, permission);
		}
	}
	private void checkAndSetPolicyPermissionsConstraints(QueryDataOfferModel build, JsonNode permissions) {

		if (permissions != null && permissions.isArray()) {
			permissions.forEach(permission -> {
				checkAndSetPolicyPermissionConstraints(build, permission);
			});
		} else {
			checkAndSetPolicyPermissionConstraints(build, permissions);
		}
	}
	
	
	private void checkAndSetPolicyPermissionConstraints(QueryDataOfferModel build, JsonNode permission) {

		JsonNode constraints = permission.get("odrl:constraint");

		List<Policies> usagePolicies = new ArrayList<>();

		if (constraints != null) {
			JsonNode jsonNode = constraints.get("odrl:and");

			if (jsonNode != null && jsonNode.isArray()) {
				jsonNode.forEach(constraint -> setConstraint(usagePolicies, constraint));
			} else if (jsonNode != null) {
				setConstraint(usagePolicies, jsonNode);
			}
		}

		build.setPolicy(PolicyModel.builder().accessPolicies(null).usagePolicies(usagePolicies).build());
	}

	private void setConstraint(List<Policies> usagePolicies, JsonNode jsonNode) {

		// All policy recieved in catalog are usage policy ,
		// accespoliocy already applied for access control,
		// in this constrain all are usage policy

		JsonNode letfOerand = jsonNode.get("odrl:leftOperand");
		String leftOperand = "";
		if(letfOerand.isObject()) {
			leftOperand = getFieldFromJsonNode(letfOerand, "@id");
		} else {
			leftOperand = getFieldFromJsonNode(jsonNode, "odrl:leftOperand");
		}
		
		String rightOperand = getFieldFromJsonNode(jsonNode, "odrl:rightOperand");
		Policies policyResponse = UtilityFunctions.identyAndGetUsagePolicy(leftOperand, rightOperand);
		if (policyResponse != null)
			usagePolicies.add(policyResponse);
	}

	private String getFieldFromJsonNode(JsonNode jnode, String fieldName) {
		if (jnode.get(fieldName) != null)
			return jnode.get(fieldName).asText();
		else
			return "";
	}

}