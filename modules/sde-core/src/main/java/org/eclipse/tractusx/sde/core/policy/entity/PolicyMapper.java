/********************************************************************************
 * Copyright (c) 2024 T-Systems International GmbH
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.core.policy.entity;

import org.eclipse.tractusx.sde.common.entities.PolicyTemplateRequest;
import org.eclipse.tractusx.sde.common.entities.SubmodelJsonRequest;
import org.eclipse.tractusx.sde.common.entities.PolicyModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PolicyMapper {

	PolicyModel mapFrom(PolicyEntity entiry);

	PolicyEntity mapFrom(PolicyModel pojo);
	
	PolicyModel mapFrom(PolicyTemplateRequest request);
	
	PolicyTemplateRequest mapFrom(SubmodelJsonRequest request);
}
