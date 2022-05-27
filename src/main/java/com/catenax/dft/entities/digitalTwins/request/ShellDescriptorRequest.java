/*
 * Copyright 2022 CatenaX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.catenax.dft.entities.digitaltwins.request;

import com.catenax.dft.entities.digitaltwins.common.Description;
import com.catenax.dft.entities.digitaltwins.common.GlobalAssetId;
import com.catenax.dft.entities.digitaltwins.common.KeyValuePair;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ShellDescriptorRequest {

    private String idShort;
    private String identification;
    private List<Description> description;
    private GlobalAssetId globalAssetId;
    private List<KeyValuePair> specificAssetIds;

    @SneakyThrows
    public String toJsonString() {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
}
