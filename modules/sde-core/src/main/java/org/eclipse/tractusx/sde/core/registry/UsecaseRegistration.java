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

package org.eclipse.tractusx.sde.core.registry;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.tractusx.sde.common.mapper.UsecaseMapper;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsecaseRegistration {

	private List<Map<Object, Object>> useCases;

	private final UsecaseMapper usecaseMapper;

	public void register(JsonArray useCaseSchema) {
		this.useCases = usecaseMapper.jsonPojoToMap(useCaseSchema);
	}

	public List<Map<Object, Object>> getUsecases() {
		return this.useCases;
	}

	@SuppressWarnings("unchecked")
	public Set<String> neededSubmodelList(List<String> selectedUsecases) {
		Set<String> listofsubmodel = new LinkedHashSet<>();
		this.useCases.stream().filter(usecase -> selectedUsecases.contains(usecase.get("id").toString())).toList()
				.forEach(obj -> listofsubmodel.addAll((ArrayList<String>) obj.get("submodules")));
		return listofsubmodel;
	}

}
