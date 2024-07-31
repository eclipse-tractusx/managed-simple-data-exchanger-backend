/********************************************************************************
 * Copyright (c) 2022, 2024 T-Systems International GmbH
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.edc.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.eclipse.tractusx.sde.common.entities.Policies;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ConstraintRequest;
import org.eclipse.tractusx.sde.edc.enums.Type;

import lombok.SneakyThrows;

public class UtilityFunctions {

	/*
	 * IMP: Resolve SonarQube Code smell Issue for
	 * "Add a private constructor to hide the implicit public one"
	 * 
	 */

	private UtilityFunctions() {
	}

	public static String removeLastSlashOfUrl(String url) {
		url = url.trim();
		if (url.endsWith("/")) {
			return url.substring(0, url.lastIndexOf("/"));
		} else {
			return url;
		}
	}

	public static List<Policies> getUsagePolicies(List<Policies> usagePolicies, List<ConstraintRequest> constraints) {
		constraints.forEach(constraint -> {
			String leftExpVal = constraint.getLeftOperand().getId();
			String rightExpVal = constraint.getRightOperand().toString();
			Policies policyResponse = identyAndGetUsagePolicy(leftExpVal, rightExpVal);
			if (policyResponse != null)
				usagePolicies.add(policyResponse);
		});
		return usagePolicies;
	}

	public static Policies identyAndGetUsagePolicy(String leftExpVal, String rightExpVal) {
		return Policies.builder().technicalKey(leftExpVal).value(List.of(rightExpVal)).build();
	}

	public static boolean checkTypeOfConnector(String type) {
		return StringUtils.isBlank(type) || Type.PROVIDER.name().equals(type);
	}

	@SneakyThrows
	public static String valueReplacer(String requestTemplatePath, Map<String, String> inputData) {
		StringSubstitutor stringSubstitutor1 = new StringSubstitutor(inputData);
		return stringSubstitutor1.replace(requestTemplatePath);
	}

}
