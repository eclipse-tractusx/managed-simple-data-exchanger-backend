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

package org.eclipse.tractusx.sde.edc.mapper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tractusx.sde.edc.entities.request.policies.ConstraintRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PermissionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyRequestFactory;
import org.eclipse.tractusx.sde.edc.model.policies.PolicyDefinition;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ContractPolicyMapper {

    @Autowired
    PolicyRequestFactory policyRequestFactory;

    public PolicyDefinition preparePolicy(String assetId, List<ConstraintRequest> constraintRequests) {

        ArrayList<PermissionRequest> permissions = new ArrayList<>();
        permissions.addAll(policyRequestFactory.getPermissions(assetId, constraintRequests));

        return PolicyDefinition.builder().permissions(permissions)
                .prohibitions(new ArrayList<>()).obligations(new ArrayList<>()).build();
    }
}
