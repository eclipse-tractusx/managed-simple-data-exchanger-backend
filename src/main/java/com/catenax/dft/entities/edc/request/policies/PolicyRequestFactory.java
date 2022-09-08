/*
 * Copyright 2022 CatenaX
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.catenax.dft.entities.edc.request.policies;

import com.catenax.dft.usecases.common.UUIdGenerator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class PolicyRequestFactory {

    public PolicyDefinitionRequest getPolicy(String shellId, String subModelId, List<ConstraintRequest> constraints, HashMap<String, String> extensibleProperties) {
        String assetId = shellId + "-" + subModelId;

        ArrayList<PermissionRequest> permissions = getPermissions(assetId, constraints);
        HashMap<String, String> type = new HashMap<>();
        type.put("@policytype", "set");

        return PolicyDefinitionRequest.builder()
                .uid(UUIdGenerator.getUrnUuid())
                .permissions(permissions)
                .prohibitions(new ArrayList<>())
                .obligations(new ArrayList<>())
                .extensibleProperties(extensibleProperties)
                .inheritsFrom(null)
                .assigner(null)
                .assignee(null)
                .target(assetId).type(type).build();
    }

    private ArrayList<PermissionRequest> getPermissions(String assetId, List<ConstraintRequest> constraints) {
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
