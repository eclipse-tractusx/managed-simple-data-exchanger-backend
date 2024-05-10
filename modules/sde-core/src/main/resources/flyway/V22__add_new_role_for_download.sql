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
	VALUES ('consumer_subscribe_download_data_offers','Allows consumer user to subscribe and download data');
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('consumer_download_data_offer','Allows consumer user to download data again');
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('consumer_view_download_history','Allows consumer user to view download data history');	
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('consumer_subscribe_download_data_offers','Creator');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('consumer_download_data_offer','Creator');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('consumer_view_download_history','Creator');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('consumer_download_data_offer','User');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('consumer_view_download_history','User');