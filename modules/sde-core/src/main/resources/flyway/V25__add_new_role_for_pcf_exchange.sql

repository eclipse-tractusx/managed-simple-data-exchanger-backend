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
	VALUES ('search_pcf','Search PCF value in consumer section');
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('request_for_pcf_value','Request for PCF value');
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('action_on_pcf_request','Action on PCF request');
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('view_pcf_history','View PCF history in provider and consumer');

	
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('search_pcf','User');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('view_pcf_history','User');
	
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('action_on_pcf_request','Admin');
	
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('search_pcf','Creator');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('request_for_pcf_value','Creator');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('view_pcf_history','Creator');