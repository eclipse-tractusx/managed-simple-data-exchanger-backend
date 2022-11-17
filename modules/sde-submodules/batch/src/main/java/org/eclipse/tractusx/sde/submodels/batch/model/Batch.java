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

package org.eclipse.tractusx.sde.submodels.batch.model;

import java.util.List;

import org.eclipse.tractusx.sde.common.entities.UsagePolicy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Batch {

    private String shellId;
    private String subModelId;
    private int rowNumber;
    private String uuid;
    private String processId;
    private String accessPolicyId;
    private String assetId; 
    private String contractDefinationId;
    private String usagePolicyId;
    private String deleted;

    private List<String> bpnNumbers;
    
    private String typeOfAccess;
    
    private List<UsagePolicy> usagePolicies;

    private String batchId;

    private String manufacturingDate;

    private String manufacturingCountry;

    private String manufacturerPartId;

    private String customerPartId;

    private String classification;

    private String nameAtManufacturer;

    private String nameAtCustomer;

    private String optionalIdentifierKey;

    private String optionalIdentifierValue;


    public boolean hasOptionalIdentifier() {
        boolean hasKey = this.getOptionalIdentifierKey() != null && !this.getOptionalIdentifierKey().isBlank();
        boolean hasValue = this.getOptionalIdentifierValue() != null && !this.getOptionalIdentifierValue().isBlank();

        return hasKey && hasValue;
    }
}
