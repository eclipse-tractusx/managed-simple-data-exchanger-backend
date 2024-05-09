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
package org.eclipse.tractusx.sde.common.constants;

public class SubmoduleCommonColumnsConstant {

	private SubmoduleCommonColumnsConstant() {
		throw new IllegalStateException("Constant class");
	}

	public static final String SHELL_ID = "shell_id";
	public static final String SHELL_ACCESS_RULE_IDS = "shell_access_rule_ids";
	public static final String SUBMODULE_ID = "sub_model_id";
	public static final String ASSET_ID = "asset_id";
	public static final String USAGE_POLICY_ID = "usage_policy_id";
	public static final String ACCESS_POLICY_ID = "access_policy_id";
	public static final String CONTRACT_DEFINATION_ID = "contract_defination_id";

	public static final String PROCESS_ID = "process_id";
	public static final String DELETED = "deleted";
	public static final String UPDATED = "updated";
	public static final String MANUFACTURER_PART_ID_FIELD = "manufacturer_part_id";

}