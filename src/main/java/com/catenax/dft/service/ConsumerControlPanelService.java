/********************************************************************************
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
import com.catenax.dft.enums.PolicyAccessEnum;
import com.catenax.dft.enums.UsagePolicyEnum;
import com.catenax.dft.facilitator.AbstractEDCStepsHelper;
import com.catenax.dft.mapper.EDCAssetConstant;
import com.catenax.dft.model.asset.Asset;
import com.catenax.dft.model.contractoffers.ContractOffer;
import com.catenax.dft.model.contractoffers.ContractOffersCatalogResponse;
import com.catenax.dft.model.policies.Constraint;
import com.catenax.dft.model.policies.PolicyDefinition;
import com.catenax.dft.model.response.QueryDataOfferModel;
import com.catenax.dft.model.response.UsagePolicyResponse;
import com.catenax.dft.util.UtilityFunctions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConsumerControlPanelService extends AbstractEDCStepsHelper {
    
	private final String edcDataUri;
    private final ContractOfferCatalogApi contractOfferCatalogApiProxy;

    @Autowired
    public ConsumerControlPanelService(@Value("${edc.consumer.datauri}") String edcDataUri,
                                       ContractOfferCatalogApi contractOfferCatalogApiProxy) {
        this.edcDataUri = edcDataUri;
        this.contractOfferCatalogApiProxy = contractOfferCatalogApiProxy;
    }

    public List<QueryDataOfferModel> queryOnDataOffers(String providerUrl) {
        providerUrl = UtilityFunctions.removeLastSlashOfUrl(providerUrl);

        providerUrl += edcDataUri;

        List<QueryDataOfferModel> queryOfferResponse = new ArrayList<>();

        ContractOffersCatalogResponse contractOfferCatalog = contractOfferCatalogApiProxy.getContractOffersCatalog(
                getAuthHeader(),
                providerUrl);

        for (ContractOffer contractOffer : contractOfferCatalog.getContractOffers()) {
            Asset asset = contractOffer.getAsset();
            PolicyDefinition policy = contractOffer.getPolicy();

            //Populating usage policies response based on usage policy constraints
            List<UsagePolicyResponse> usagePolicies = new ArrayList<>();
            policy.getPermissions().stream().forEach(permission -> {
                usagePolicies.addAll(getUsagePolicies(permission.getConstraints().stream()));
            });

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
                            .policyId(contractOffer.getPolicy().getUid())
                            .build()
                    );
        }
        return queryOfferResponse;
    }

    private void addMissingPolicies(List<UsagePolicyResponse> usagePolicies) {
        Arrays.stream(UsagePolicyEnum.values()).forEach(
                policy -> {
                    if (!policy.equals(UsagePolicyEnum.CUSTOM)) {
                        boolean found = usagePolicies.stream().anyMatch(usagePolicy -> usagePolicy.getType().equals(policy));
                        if (!found) {
                            UsagePolicyResponse policyObj = UsagePolicyResponse.builder().type(policy).typeOfAccess(PolicyAccessEnum.UNRESTRICTED)
                                    .value("").build();
                            usagePolicies.add(policyObj);
                        }
                    }
                }
        );
    }

    private String getFieldFromAsset(Asset asset, String field) {
        return asset.getProperties().getOrDefault(field, "");
    }

    private List<UsagePolicyResponse> getUsagePolicies(Stream<Constraint> constraints) {
        List<UsagePolicyResponse> usagePolicies = new ArrayList<>();
        constraints.forEach(constraint ->
        {
            Object leftExpVal = constraint.getLeftExpression().getValue();
            Object rightExpVal = constraint.getRightExpression().getValue();
            UsagePolicyResponse policyResponse = null;
            switch (leftExpVal.toString()) {
                case "idsc:ROLE":
                    policyResponse = UsagePolicyResponse.builder().type(UsagePolicyEnum.ROLE)
                            .typeOfAccess(PolicyAccessEnum.RESTRICTED)
                            .value(rightExpVal.toString())
                            .build();
                    usagePolicies.add(policyResponse);
                    break;
                case "idsc:ELAPSED_TIME":
                    policyResponse = UsagePolicyResponse.builder().type(UsagePolicyEnum.DURATION)
                            .typeOfAccess(PolicyAccessEnum.RESTRICTED)
                            .value(UtilityFunctions.getDurationValue(rightExpVal.toString()))
                            .build();
                    usagePolicies.add(policyResponse);
                    break;
                case "idsc:PURPOSE":
                    policyResponse = UsagePolicyResponse.builder().type(UsagePolicyEnum.PURPOSE)
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

}
