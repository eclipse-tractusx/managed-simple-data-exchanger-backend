/********************************************************************************
 * Copyright (c) 2022 T-Systems International Gmbh
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

package org.eclipse.tractusx.sde.edc.entities.request.policies.usagepolicy;

import org.eclipse.tractusx.sde.common.entities.UsagePolicies;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ConstraintRequest;
import org.eclipse.tractusx.sde.edc.exceptions.EDCException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@SuperBuilder
public class CustomPolicyDTO extends UsagePolicyDTO{
	
    private static final String DATASPACECONNECTOR_LITERALEXPRESSION = "dataspaceconnector:literalexpression";

    public static CustomPolicyDTO fromUsagePolicy(UsagePolicies usagePolicy)
    {
        return CustomPolicyDTO.builder().type(usagePolicy.getType()).typeOfAccess(usagePolicy.getTypeOfAccess())
                .value(usagePolicy.getValue()).build();

    }
    @Override
    public ConstraintRequest toConstraint() {
        throw new EDCException("Constraint not supported for custom policy");
    }
}
