/********************************************************************************
 * Copyright (c) 2022, 2023 T-Systems International GmbH
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

CREATE TABLE sde_role
(
    sde_role      VARCHAR(200),
    description   VARCHAR(400),
    PRIMARY KEY (sde_role)
);

CREATE TABLE sde_permission
(
    sde_permission   VARCHAR(200),
    description   VARCHAR(400),
    PRIMARY KEY (sde_permission)
);


-- public.sde_role_permission_mapping definition

CREATE TABLE sde_role_permission_mapping (
	sde_permission varchar(255) NOT NULL,
	sde_role varchar(255) NOT NULL,
	CONSTRAINT sde_role_permission_mapping_pkey PRIMARY KEY (sde_permission, sde_role)
);


-- sde_role_permission_mapping foreign keys

ALTER TABLE sde_role_permission_mapping 
ADD CONSTRAINT sde_role_permission_mapping_fk FOREIGN KEY (sde_role) 
REFERENCES sde_role(sde_role) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE sde_role_permission_mapping ADD 
CONSTRAINT sde_role_permission_mapping_fk_1 
FOREIGN KEY (sde_permission) 
REFERENCES sde_permission(sde_permission) ON DELETE CASCADE ON UPDATE CASCADE;


/** Insert role ******/
INSERT INTO sde_role (sde_role,description)
	VALUES ('User','User role has limited permission for SDE');
INSERT INTO sde_role (sde_role,description)
	VALUES ('Admin','Admin as all permission');
INSERT INTO sde_role (sde_role,description)
	VALUES ('Creator','Creator only create permission');
	

/** Insert permission ******/
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('provider_create_contract_offer','Allows user to provide data for a new Contract Offer - using any of the different mechanisms to provide data via SDE (file upload, tabular entry, JSON entry) including the definition of policies');
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('provider_update_contract_offer','Allows user to edit/update data for an existing Contract Offer - including the modification of policies');
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('provider_delete_contract_offer','Allows user to delete an existing Contract Offer - this includes deleting the data and associated policies');
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('provider_view_history','Allows user to view history of created Contract Offers');
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('provider_download_own_data','Allows user to download raw data associated with an existing Contract Offer	');
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('provider_view_contract_agreement','Allows user to view existing contract agreements including their details (status etc.)');
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('provider_delete_contract_agreement','Allows user to cancel/terminate existing contract agreement');
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('consumer_search_connectors','Allows user to find connectors within the Catena-X network using company name or BPN');
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('consumer_view_contract_offers','Allows user to request and view the catalog of contract offers of a chosen provider connector');
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('consumer_establish_contract_agreement','Allows user to trigger the negotiation of a contract agreement for a given contract offer of a provider connector');
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('consumer_view_contract_agreement','Allows user to view existing contract agreements including their details (status etc.)');
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('consumer_delete_contract_agreement','Allows user to cancel/terminate existing contract agreement');
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('create_role','Allows user to create_role');
INSERT INTO sde_permission (sde_permission,description)
	VALUES ('read_role_permission','Allows user to read_role_permission');


/** Insert role and permission mapping ******/
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('provider_create_contract_offer','Creator');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('provider_update_contract_offer','Creator');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('provider_delete_contract_offer','Admin');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('provider_view_history','User');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('provider_view_history','Creator');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('provider_download_own_data','User');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('provider_download_own_data','Creator');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('provider_view_contract_agreement','User');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('provider_view_contract_agreement','Creator');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('provider_delete_contract_agreement','Admin');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('consumer_search_connectors','User');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('consumer_search_connectors','Creator');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('consumer_view_contract_offers','User');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('consumer_view_contract_offers','Creator');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('consumer_establish_contract_agreement','Creator');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('consumer_view_contract_agreement','User');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('consumer_view_contract_agreement','Creator');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('consumer_delete_contract_agreement','Admin');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('create_role','Admin');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('read_role_permission','Admin');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('create_role','User');
INSERT INTO sde_role_permission_mapping (sde_permission,sde_role)
	VALUES ('read_role_permission','User');

