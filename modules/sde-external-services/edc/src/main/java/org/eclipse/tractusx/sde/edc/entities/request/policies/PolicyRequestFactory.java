/********************************************************************************
 * Copyright (c) 2022 BMW GmbH
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

package org.eclipse.tractusx.sde.edc.entities.request.policies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.edc.util.UUIdGenerator;
import org.springframework.stereotype.Service;

@Service
public class PolicyRequestFactory {

    public PolicyDefinitionRequest getPolicy(String shellId, String subModelId, List<ConstraintRequest> constraints, Map<String, String> extensibleProperties) {
        String assetId = shellId + "-" + subModelId;

        List<PermissionRequest> permissions = getPermissions(assetId, constraints);
        HashMap<String, String> type = new HashMap<>();
        type.put("@policytype", "set");
        PolicyRequest policyRequest = PolicyRequest.builder().permissions(permissions).obligations(new ArrayList<>())
                .extensibleProperties(extensibleProperties).inheritsFrom(null).assignee(null).assigner(null)
                .target(assetId).type(type).prohibitions(new ArrayList<>()).build();

        return PolicyDefinitionRequest.builder()
                .id(UUIdGenerator.getUrnUuid())
                .policyRequest(policyRequest)
                .build();
    }

    public List<PermissionRequest> getPermissions(String assetId, List<ConstraintRequest> constraints) {
        ArrayList<PermissionRequest> permissions = new ArrayList<>();
        ActionRequest action = ActionRequest.builder()
                .type("USE")
                .includedIn(null)
                .constraint(null)
                .build();

        PermissionRequest permissionRequest = PermissionRequest.builder()
                .target(assetId)
                .action(action)
                .assignee(null)
                .assigner(null)
                .constraints(constraints)
                .duties(new ArrayList<>())
                .edcType("dataspaceconnector:permission")
                .build();
        permissions.add(permissionRequest);
        return permissions;
    }
}