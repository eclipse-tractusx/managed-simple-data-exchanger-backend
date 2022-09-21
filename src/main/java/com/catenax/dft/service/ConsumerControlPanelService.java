/********************************************************************************
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the CatenaX (ng) GitHub Organisation
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

package com.catenax.dft.service;

import com.catenax.dft.api.ContractOfferCatalogApi;
import com.catenax.dft.entities.UsagePolicy;
import com.catenax.dft.entities.database.ContractNegotiationInfoEntity;
import com.catenax.dft.entities.edc.request.policies.ConstraintRequest;
import com.catenax.dft.entities.edc.request.policies.PolicyConstraintBuilderService;
import com.catenax.dft.enums.PolicyAccessEnum;
import com.catenax.dft.enums.UsagePolicyEnum;
import com.catenax.dft.facilitator.AbstractEDCStepsHelper;
import com.catenax.dft.facilitator.ContractNegotiateManagement;
import com.catenax.dft.gateways.database.ContractNegotiationInfoRepository;
import com.catenax.dft.mapper.EDCAssetConstant;
import com.catenax.dft.model.asset.Asset;
import com.catenax.dft.model.contractnegotiation.ContractNegotiationsResponse;
import com.catenax.dft.model.contractoffers.ContractOffer;
import com.catenax.dft.model.contractoffers.ContractOffersCatalogResponse;
import com.catenax.dft.model.policies.PolicyDefinition;
import com.catenax.dft.model.request.ConsumerRequest;
import com.catenax.dft.model.response.QueryDataOfferModel;
import com.catenax.dft.util.UtilityFunctions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@Slf4j
@Service
public class ConsumerControlPanelService extends AbstractEDCStepsHelper {

    private final Integer limit = 10000;
    private final String edcDataUri;
    private final ContractOfferCatalogApi contractOfferCatalogApiProxy;
    private final ContractNegotiateManagement contractNegotiateManagement;

    private ContractNegotiationInfoRepository contractNegotiationInfoRepository;
    private PolicyConstraintBuilderService policyConstraintBuilderService;


    @Autowired
    public ConsumerControlPanelService(@Value("${edc.consumer.datauri}") String edcDataUri,
                                       ContractOfferCatalogApi contractOfferCatalogApiProxy, ContractNegotiateManagement contractNegotiateManagement, ContractNegotiationInfoRepository contractNegotiationInfoRepository, PolicyConstraintBuilderService policyConstraintBuilderService) {
        this.edcDataUri = edcDataUri;
        this.contractOfferCatalogApiProxy = contractOfferCatalogApiProxy;
        this.contractNegotiateManagement = contractNegotiateManagement;
        this.contractNegotiationInfoRepository = contractNegotiationInfoRepository;
        this.policyConstraintBuilderService = policyConstraintBuilderService;
    }

    public List<QueryDataOfferModel> queryOnDataOffers(String providerUrl) {
        providerUrl = UtilityFunctions.removeLastSlashOfUrl(providerUrl);

        providerUrl += edcDataUri;

        List<QueryDataOfferModel> queryOfferResponse = new ArrayList<>();

        ContractOffersCatalogResponse contractOfferCatalog = contractOfferCatalogApiProxy.getContractOffersCatalog(
                getAuthHeader(),
                providerUrl, limit);

        for (ContractOffer contractOffer : contractOfferCatalog.getContractOffers()) {
            Asset asset = contractOffer.getAsset();
            PolicyDefinition policy = contractOffer.getPolicy();

            //Populating usage policies response based on usage policy constraints
            List<UsagePolicy> usagePolicies = new ArrayList<>();
            policy.getPermissions().stream().forEach(permission -> {
                usagePolicies.addAll(getUsagePolicies(permission.getConstraints().stream()));
            });

            addCustomUsagePolicy(policy.getExtensibleProperties(), usagePolicies);
            //Later to be part of access policy
            List<String> bpnNumbers = new ArrayList<>();
            policy.getPermissions().stream().forEach(permission -> {
                permission.getConstraints().stream().forEach(constraint -> {
                    if (constraint.getLeftExpression().getValue().equals("BusinessPartnerNumber")) {
                        String value = constraint.getRightExpression().getValue().toString();
                        bpnNumbers.addAll(Arrays.asList(value.trim().substring(value.indexOf("[") + 1, value.indexOf("]")).split(",")));
                        return;
                    }
                });
            });

            queryOfferResponse.add(QueryDataOfferModel.builder().assetId(getFieldFromAsset(asset, EDCAssetConstant.ASSET_PROP_ID))
                    .connectorOfferUrl(providerUrl + File.separator + getFieldFromAsset(asset, EDCAssetConstant.ASSET_PROP_ID))
                    .offerId(contractOffer.getId())
                    .title(getFieldFromAsset(asset, EDCAssetConstant.ASSET_PROP_NAME))
                    .description(getFieldFromAsset(asset, EDCAssetConstant.ASSET_PROP_DESCRIPTION))
                    .created(getFieldFromAsset(asset, EDCAssetConstant.ASSET_PROP_CREATED))
                    .modified(getFieldFromAsset(asset, EDCAssetConstant.ASSET_PROP_MODIFIED))
                    .publisher(getFieldFromAsset(asset, EDCAssetConstant.ASSET_PROP_PUBLISHER))
                    .typeOfAccess(!bpnNumbers.isEmpty() ? PolicyAccessEnum.RESTRICTED : PolicyAccessEnum.UNRESTRICTED)
                    .version(getFieldFromAsset(asset, EDCAssetConstant.ASSET_PROP_VERSION))
                    .bpnNumbers(bpnNumbers)
                    .usagePolicies(usagePolicies)
                    .fileName(getFieldFromAsset(asset, EDCAssetConstant.ASSET_PROP_FILENAME))
                    .fileContentType(getFieldFromAsset(asset, EDCAssetConstant.ASSET_PROP_CONTENTTYPE))
                    .connectorId(contractOfferCatalog.getId())
                    .build()
            );
        }
        return queryOfferResponse;
    }

    private void addMissingPolicies(List<UsagePolicy> usagePolicies) {
        Arrays.stream(UsagePolicyEnum.values()).forEach(
                policy -> {
                    if (!policy.equals(UsagePolicyEnum.CUSTOM)) {
                        boolean found = usagePolicies.stream().anyMatch(usagePolicy -> usagePolicy.getType().equals(policy));
                        if (!found) {
                            UsagePolicy policyObj = UsagePolicy.builder().type(policy).typeOfAccess(PolicyAccessEnum.UNRESTRICTED)
                                    .value("").build();
                            usagePolicies.add(policyObj);
                        }
                    }
                }
        );
    }

    private void addCustomUsagePolicy(HashMap<String, String> extensibleProperties, List<UsagePolicy> usagePolicies) {
        if (!CollectionUtils.isEmpty(extensibleProperties) &&
                extensibleProperties.keySet().contains(UsagePolicyEnum.CUSTOM.name())) {
            UsagePolicy policyObj = UsagePolicy.builder().type(UsagePolicyEnum.CUSTOM).typeOfAccess(PolicyAccessEnum.RESTRICTED)
                    .value(extensibleProperties.get(UsagePolicyEnum.CUSTOM.name())).build();
            usagePolicies.add(policyObj);
        }
        else
        {
            UsagePolicy policyObj = UsagePolicy.builder().type(UsagePolicyEnum.CUSTOM).typeOfAccess(PolicyAccessEnum.UNRESTRICTED)
                    .value("").build();
            usagePolicies.add(policyObj);
        }
    }

    private String getFieldFromAsset(Asset asset, String field) {
        return asset.getProperties().getOrDefault(field, "");
    }

    private List<UsagePolicy> getUsagePolicies(Stream<ConstraintRequest> constraints) {
        List<UsagePolicy> usagePolicies = new ArrayList<>();
        constraints.forEach(constraint ->
        {
            Object leftExpVal = constraint.getLeftExpression().getValue();
            Object rightExpVal = constraint.getRightExpression().getValue();
            UsagePolicy policyResponse = null;
            switch (leftExpVal.toString()) {
                case "idsc:ROLE":
                    policyResponse = UsagePolicy.builder().type(UsagePolicyEnum.ROLE)
                            .typeOfAccess(PolicyAccessEnum.RESTRICTED)
                            .value(rightExpVal.toString())
                            .build();
                    usagePolicies.add(policyResponse);
                    break;
                case "idsc:ELAPSED_TIME":
                    policyResponse = UtilityFunctions.getDurationPolicy(rightExpVal.toString());
                    usagePolicies.add(policyResponse);
                    break;
                case "idsc:PURPOSE":
                    policyResponse = UsagePolicy.builder().type(UsagePolicyEnum.PURPOSE)
                            .typeOfAccess(PolicyAccessEnum.RESTRICTED)
                            .value(rightExpVal.toString())
                            .build();
                    usagePolicies.add(policyResponse);
                    break;
                default:
                    break;
            }
        });
        addMissingPolicies(usagePolicies);
        return usagePolicies;
    }

    @Async
    public void subscribeDataOffers(ConsumerRequest consumerRequest, String processId) {
        HashMap<String, String> extensibleProperty = new HashMap<>();
        String recipient = UtilityFunctions.removeLastSlashOfUrl(consumerRequest.getProviderUrl());
        AtomicReference<String> negotiateContractId = new AtomicReference<>();
        AtomicReference<ContractNegotiationsResponse> checkContractNegotiationStatus = new AtomicReference<>();
        var recipientURL = recipient + edcDataUri;
        List<UsagePolicy> policies = consumerRequest.getPolicies();
        UsagePolicy customPolicy = policies.stream().filter(type-> type.getType().equals(UsagePolicyEnum.CUSTOM)).findFirst().get();
        if(StringUtils.isNotBlank(customPolicy.getValue()))
        {
            extensibleProperty.put(customPolicy.getType().name(), customPolicy.getValue());
        }
        List<ConstraintRequest> constraintRequests = policyConstraintBuilderService.getUsagePolicyConstraints(policies);
        consumerRequest.getOffers().parallelStream().forEach((offer) -> {
            try {

                negotiateContractId.set(contractNegotiateManagement.negotiateContract(offer.getOfferId(),
                        recipientURL, offer.getAssetId(), constraintRequests, extensibleProperty));
                int retry = 3;
                int counter = 1;

                do {
                    Thread.sleep(3000);
                    checkContractNegotiationStatus.set(contractNegotiateManagement
                            .checkContractNegotiationStatus(negotiateContractId.get()));
                    counter++;
                } while (checkContractNegotiationStatus.get() != null && !checkContractNegotiationStatus.get().getState()
                        .equals("CONFIRMED") && !checkContractNegotiationStatus.get().getState().equals("DECLINED") && counter <= retry);


            } catch (Exception e) {
                log.error("Exception in subscribeDataOffers" + e.getMessage());
            } finally {
                // Local DB entry
                ContractNegotiationInfoEntity contractNegotiationInfoEntity = ContractNegotiationInfoEntity.builder()
                        .processId(processId)
                        .connectorId(consumerRequest.getConnectorId())
                        .offerId(offer.getOfferId())
                        .contractNegotiationId(negotiateContractId != null ? negotiateContractId.get() : null)
                        .status(checkContractNegotiationStatus.get()!=null ? checkContractNegotiationStatus.get().getState():"Failed:Exception")
                        .dateTime(LocalDateTime.now()).build();
                contractNegotiationInfoRepository.save(contractNegotiationInfoEntity);
            }
        });

    }

}
