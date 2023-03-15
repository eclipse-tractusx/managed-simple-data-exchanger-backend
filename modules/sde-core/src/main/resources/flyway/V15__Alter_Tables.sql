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

ALTER TABLE IF EXISTS aspect
ADD COLUMN sub_model_id                  		TEXT NULL;

ALTER TABLE IF EXISTS aspect_relationship
ADD COLUMN sub_model_id                  		TEXT NULL;

ALTER TABLE IF EXISTS batch
ADD COLUMN sub_model_id                  		TEXT NULL;

ALTER TABLE IF EXISTS part_as_planned
ADD COLUMN sub_model_id                  		TEXT NULL;
		
ALTER TABLE IF EXISTS Part_site_information_as_planned
ADD COLUMN sub_model_id                  		TEXT NULL;

ALTER TABLE IF EXISTS single_level_bom_as_planned
ADD COLUMN sub_model_id                  		TEXT NULL;

ALTER TABLE IF EXISTS aspect_relationship
ADD COLUMN parent_part_instance_id  			TEXT NULL;

ALTER TABLE IF EXISTS aspect_relationship
ADD COLUMN parent_manufacturer_part_id  		TEXT NULL;

ALTER TABLE IF EXISTS aspect_relationship 
ADD COLUMN parent_optional_identifier_key  		TEXT NULL;

ALTER TABLE IF EXISTS aspect_relationship
ADD COLUMN parent_optional_identifier_value 	TEXT NULL;

ALTER TABLE IF EXISTS aspect_relationship
ADD COLUMN part_instance_id  					TEXT NULL;

ALTER TABLE IF EXISTS aspect_relationship
ADD COLUMN manufacturer_part_id  				TEXT NULL;

ALTER TABLE IF EXISTS aspect_relationship 
ADD COLUMN optional_identifier_key  			TEXT NULL;

ALTER TABLE IF EXISTS aspect_relationship
ADD COLUMN optional_identifier_value 			TEXT NULL;

ALTER TABLE IF EXISTS aspect_relationship 
RENAME COLUMN parent_catenax_id TO parent_uuid;

ALTER TABLE IF EXISTS aspect_relationship
RENAME COLUMN child_catenax_id TO uuid;

ALTER TABLE IF EXISTS aspect_relationship
RENAME COLUMN data_type_uri TO datatype_uri;

ALTER TABLE IF EXISTS single_level_bom_as_planned
RENAME COLUMN parent_catenax_id TO parent_uuid;

ALTER TABLE IF EXISTS single_level_bom_as_planned
RENAME COLUMN child_catenax_id TO uuid;

ALTER TABLE IF EXISTS single_level_bom_as_planned
ADD COLUMN manufacturer_part_id  				TEXT NULL;

ALTER TABLE IF EXISTS single_level_bom_as_planned
ADD COLUMN parent_manufacturer_part_id  		TEXT NULL;

ALTER TABLE IF EXISTS single_level_bom_as_planned
ADD COLUMN customer_part_id  					TEXT NULL;








