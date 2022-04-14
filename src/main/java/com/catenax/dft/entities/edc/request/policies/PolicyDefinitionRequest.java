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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PolicyDefinitionRequest {
    private String uid;
    private ArrayList<PermissionRequest> permissions;
    private ArrayList<ProhibitionRequest> prohibitions;
    private ArrayList<ObligationRequest> obligations;
    private HashMap<String, String> extensibleProperties;
    private String inheritsFrom;
    private String assigner;
    private String assignee;
    private String target;
    @JsonProperty("@type")
    private String type;

    @SneakyThrows
    public String toJsonString() {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
}
