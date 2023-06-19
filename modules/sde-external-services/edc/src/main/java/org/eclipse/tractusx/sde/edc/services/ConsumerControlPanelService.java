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

package org.eclipse.tractusx.sde.edc.services;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.common.enums.PolicyAccessEnum;
import org.eclipse.tractusx.sde.common.enums.UsagePolicyEnum;
import org.eclipse.tractusx.sde.edc.api.ContractOfferCatalogApi;
import org.eclipse.tractusx.sde.edc.constants.EDCAssetConstant;
import org.eclipse.tractusx.sde.edc.entities.database.ContractNegotiationInfoEntity;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ActionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyConstraintBuilderService;
import org.eclipse.tractusx.sde.edc.facilitator.AbstractEDCStepsHelper;
import org.eclipse.tractusx.sde.edc.facilitator.ContractNegotiateManagementHelper;
import org.eclipse.tractusx.sde.edc.gateways.database.ContractNegotiationInfoRepository;
import org.eclipse.tractusx.sde.edc.model.contractnegotiation.ContractNegotiationDto;
import org.eclipse.tractusx.sde.edc.model.contractoffers.ContractOfferRequestFactory;
import org.eclipse.tractusx.sde.edc.model.request.ConsumerRequest;
import org.eclipse.tractusx.sde.edc.model.response.QueryDataOfferModel;
import org.eclipse.tractusx.sde.edc.util.UtilityFunctions;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerControlPanelService extends AbstractEDCStepsHelper {

	private final ContractOfferCatalogApi contractOfferCatalogApiProxy;
	private final ContractNegotiateManagementHelper contractNegotiateManagement;

	private final ContractNegotiationInfoRepository contractNegotiationInfoRepository;
	private final PolicyConstraintBuilderService policyConstraintBuilderService;

	private final ContractOfferRequestFactory contractOfferRequestFactory;

	public List<QueryDataOfferModel> queryOnDataOffers(String providerUrl, Integer limit, Integer offset) {

		String sproviderUrl = UtilityFunctions.removeLastSlashOfUrl(providerUrl);

		List<QueryDataOfferModel> queryOfferResponse = new ArrayList<>();

		JsonNode contractOfferCatalog = contractOfferCatalogApiProxy.getContractOffersCatalog(
				contractOfferRequestFactory.getContractOfferRequest(providerUrl, limit, offset));

		JsonNode jOffer = contractOfferCatalog.get("dcat:dataset");
		if (jOffer.isArray()) {

			jOffer.forEach(
					offer -> queryOfferResponse.add(buildContractOffer(sproviderUrl, contractOfferCatalog, offer)));

		} else {
			queryOfferResponse.add(buildContractOffer(sproviderUrl, contractOfferCatalog, jOffer));
		}

		return queryOfferResponse;
	}

	private QueryDataOfferModel buildContractOffer(String sproviderUrl, JsonNode contractOfferCatalog, JsonNode offer) {

		JsonNode policy = offer.get("odrl:hasPolicy");

		QueryDataOfferModel build = QueryDataOfferModel.builder()
				.assetId(getFieldFromJsonNode(offer, EDCAssetConstant.ASSET_PROP_ID))
				.connectorOfferUrl(sproviderUrl + File.separator + getFieldFromJsonNode(offer, "@id"))
				.offerId(getFieldFromJsonNode(policy, "@id"))
				.title(getFieldFromJsonNode(offer, EDCAssetConstant.ASSET_PROP_NAME))
				.type(getFieldFromJsonNode(offer, EDCAssetConstant.ASSET_PROP_TYPE))
				.description(getFieldFromJsonNode(offer, EDCAssetConstant.ASSET_PROP_DESCRIPTION))
				.created(getFieldFromJsonNode(offer, EDCAssetConstant.ASSET_PROP_CREATED))
				.modified(getFieldFromJsonNode(offer, EDCAssetConstant.ASSET_PROP_MODIFIED))
				.publisher(getFieldFromJsonNode(offer, EDCAssetConstant.ASSET_PROP_PUBLISHER))
				.version(getFieldFromJsonNode(offer, EDCAssetConstant.ASSET_PROP_VERSION))
				.fileName(getFieldFromJsonNode(offer, EDCAssetConstant.ASSET_PROP_FILENAME))
				.fileContentType(getFieldFromJsonNode(offer, EDCAssetConstant.ASSET_PROP_CONTENTTYPE))
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

		List<UsagePolicies> usagePolicies = new ArrayList<>();

		List<String> bpnNumbers = new ArrayList<>();

		if (constraints != null) {
			JsonNode jsonNode = constraints.get("odrl:and");

			if (jsonNode != null && jsonNode.isArray()) {
				jsonNode.forEach(constraint -> {
					setConstraint(usagePolicies, bpnNumbers, constraint);
				});
			} else if (jsonNode != null) {
				setConstraint(usagePolicies, bpnNumbers, jsonNode);
			}
		}
		build.setTypeOfAccess(!bpnNumbers.isEmpty() ? PolicyAccessEnum.RESTRICTED : PolicyAccessEnum.UNRESTRICTED);
		build.setBpnNumbers(bpnNumbers);
		build.setUsagePolicies(usagePolicies);
	}

	private void setConstraint(List<UsagePolicies> usagePolicies, List<String> bpnNumbers, JsonNode jsonNode) {

		String leftOperand = getFieldFromJsonNode(jsonNode, "odrl:leftOperand");
		String rightOperand = getFieldFromJsonNode(jsonNode, "odrl:rightOperand");

		if (leftOperand.equals("BusinessPartnerNumber")) {
			bpnNumbers.add(rightOperand);
		} else {
			usagePolicies.add(UtilityFunctions.identyAndGetUsagePolicy(leftOperand, rightOperand));
		}
	}

	private String getFieldFromJsonNode(JsonNode jnode, String fieldName) {
		if (jnode.get(fieldName) != null)
			return jnode.get(fieldName).asText();
		else
			return "";
	}

	@Async
	public void subscribeDataOffers(ConsumerRequest consumerRequest, String processId) {

		HashMap<String, String> extensibleProperty = new HashMap<>();
		AtomicReference<String> negotiateContractId = new AtomicReference<>();
		AtomicReference<ContractNegotiationDto> checkContractNegotiationStatus = new AtomicReference<>();

		var recipientURL = UtilityFunctions.removeLastSlashOfUrl(consumerRequest.getProviderUrl());

		List<UsagePolicies> policies = consumerRequest.getPolicies();

		Optional<UsagePolicies> findFirst = policies.stream()
				.filter(type -> type.getType().equals(UsagePolicyEnum.CUSTOM)).findFirst();

		if (findFirst.isPresent()) {
			extensibleProperty.put(findFirst.get().getType().name(), findFirst.get().getValue());
		}

		ActionRequest action = policyConstraintBuilderService.getUsagePolicyConstraints(policies);
		consumerRequest.getOffers().parallelStream().forEach((offer) -> {
			try {

				negotiateContractId.set(
						contractNegotiateManagement.negotiateContract(recipientURL, consumerRequest.getConnectorId(),
								offer.getOfferId(), offer.getAssetId(), action, extensibleProperty));
				int retry = 3;
				int counter = 1;

				do {
					Thread.sleep(3000);
					checkContractNegotiationStatus
							.set(contractNegotiateManagement.checkContractNegotiationStatus(negotiateContractId.get()));
					counter++;
				} while (checkContractNegotiationStatus.get() != null
						&& !checkContractNegotiationStatus.get().getState().equals("FINALIZED")
						&& !checkContractNegotiationStatus.get().getState().equals("DECLINED") && counter <= retry);

			} catch (Exception e) {
				log.error("Exception in subscribeDataOffers" + e.getMessage());
			} finally {

				// Local DB entry
				ContractNegotiationInfoEntity contractNegotiationInfoEntity = ContractNegotiationInfoEntity.builder()
						.processId(processId).connectorId(consumerRequest.getConnectorId()).offerId(offer.getOfferId())
						.contractNegotiationId(negotiateContractId != null ? negotiateContractId.get() : null)
						.status(checkContractNegotiationStatus.get() != null
								? checkContractNegotiationStatus.get().getState()
								: "Failed:Exception")
						.dateTime(LocalDateTime.now()).build();

				contractNegotiationInfoRepository.save(contractNegotiationInfoEntity);
			}
		});

	}

}
