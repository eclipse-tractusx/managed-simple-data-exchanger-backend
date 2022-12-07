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

package org.eclipse.tractusx.sde.core.controller;

import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.sde.core.service.SubmodelService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@ControllerAdvice
@RequiredArgsConstructor
public class SubmodelController {

	private final SubmodelService submodelService;

	@GetMapping("/submodels")
	public List<Map<String, String>> getAllSubmodels() {
		return submodelService.findAllSubmodels();
	}

	@GetMapping("/submodels/{submodelName}")
	public Map<Object, Object> getSubmodelByName(@PathVariable String submodelName) {
		return submodelService.findSubmodelByName(submodelName);
	}

}
