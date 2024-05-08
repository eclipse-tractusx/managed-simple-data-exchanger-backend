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
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('policyhub_view_policy_attributes','View policy attributes');
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('policyhub_view_policy_types','View policy types');
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('policyhub_view_policy_content','View policy content');
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('policyhub_policy_content','Allow user to create policy content');

	
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('policyhub_view_policy_attributes','User');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('policyhub_view_policy_types','User');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('policyhub_view_policy_content','User');
	
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('policyhub_policy_content','Admin');
	
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('policyhub_view_policy_attributes','Creator');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('policyhub_view_policy_types','Creator');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('policyhub_view_policy_content','Creator');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('policyhub_policy_content','Creator');