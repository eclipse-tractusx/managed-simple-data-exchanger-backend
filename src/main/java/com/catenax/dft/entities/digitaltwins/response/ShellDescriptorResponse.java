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

package com.catenax.dft.entities.digitaltwins.response;

import java.util.List;

import com.catenax.dft.entities.digitaltwins.common.Description;
import com.catenax.dft.entities.digitaltwins.common.GlobalAssetId;
import com.catenax.dft.entities.digitaltwins.common.KeyValuePair;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShellDescriptorResponse {
    private String idShort;
    private String identification;
    private List<Description> description;
    private GlobalAssetId globalAssetId;
    private List<KeyValuePair> specificAssetIds;
}
