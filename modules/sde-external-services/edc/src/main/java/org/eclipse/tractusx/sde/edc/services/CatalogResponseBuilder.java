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

	public List<QueryDataOfferModel> queryOnDataOffers(String providerUrl, Integer offset, Integer limit,
			String filterExpression) {

		providerUrl = UtilityFunctions.removeLastSlashOfUrl(providerUrl);

		if (!providerUrl.endsWith(protocolPath))
			providerUrl = providerUrl + protocolPath;

		String sproviderUrl = providerUrl;

		List<QueryDataOfferModel> queryOfferResponse = new ArrayList<>();

		JsonNode contractOfferCatalog = contractOfferCatalogApiProxy.getContractOffersCatalog(
				contractOfferRequestFactory.getContractOfferRequest(sproviderUrl, limit, offset, filterExpression));

		JsonNode jOffer = contractOfferCatalog.get("dcat:dataset");
		if (jOffer.isArray()) {

			jOffer.forEach(
					offer -> queryOfferResponse.add(buildContractOffer(sproviderUrl, contractOfferCatalog, offer)));

		} else {
			queryOfferResponse.add(buildContractOffer(sproviderUrl, contractOfferCatalog, jOffer));
		}

		return queryOfferResponse;
	}

	public QueryDataOfferModel buildContractOffer(String sproviderUrl, JsonNode contractOfferCatalog, JsonNode offer) {

		JsonNode policy = offer.get("odrl:hasPolicy");

		String edcstr = "edc:";

		QueryDataOfferModel build = QueryDataOfferModel.builder()
				.assetId(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_ID))
				.connectorOfferUrl(sproviderUrl).offerId(getFieldFromJsonNode(policy, "@id"))
				.title(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_NAME))
				.type(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_TYPE))
				.description(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_DESCRIPTION))
				.created(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_CREATED))
				.modified(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_MODIFIED))
				.publisher(getFieldFromJsonNode(contractOfferCatalog, edcstr + "participantId"))
				.version(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_VERSION))
				.fileName(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_FILENAME))
				.fileContentType(getFieldFromJsonNode(offer, edcstr + EDCAssetConstant.ASSET_PROP_CONTENTTYPE))
				.connectorId(getFieldFromJsonNode(contractOfferCatalog, "edc:participantId")).build();

		checkAndSetPolicyPermission(build, policy);

		return build;
	}

	private void checkAndSetPolicyPermission(QueryDataOfferModel build, JsonNode policy) {

		if (policy != null && policy.isArray()) {
			policy.forEach(pol -> {
				JsonNode permission = pol.get("odrl:permission");
				checkAndSetPolicyPermissionConstraints(build, permission);
			});
		} else if (policy != null) {
			JsonNode permission = policy.get("odrl:permission");
			checkAndSetPolicyPermissionConstraints(build, permission);
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

		String leftOperand = getFieldFromJsonNode(jsonNode, "odrl:leftOperand");
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