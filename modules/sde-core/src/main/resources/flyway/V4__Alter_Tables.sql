/********************************************************************************
 * Copyright (c) 2022 BMW GmbH
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

 ALTER TABLE aspect
    ALTER COLUMN uuid                      TYPE TEXT,
    ALTER COLUMN process_id                TYPE TEXT,
    ALTER COLUMN part_instance_id          TYPE TEXT,
    ALTER COLUMN manufacturing_date        TYPE TEXT,
    ALTER COLUMN manufacturing_country     TYPE TEXT,
    ALTER COLUMN manufacturer_part_id      TYPE TEXT,
    ALTER COLUMN customer_part_id          TYPE TEXT,
    ALTER COLUMN classification            TYPE TEXT,
    ALTER COLUMN name_at_manufacturer      TYPE TEXT,
    ALTER COLUMN name_at_customer          TYPE TEXT,
    ALTER COLUMN optional_identifier_key   TYPE TEXT,
    ALTER COLUMN optional_identifier_value TYPE TEXT,
    ALTER COLUMN shell_id                  TYPE TEXT;


 ALTER TABLE aspect_relationship
    ALTER COLUMN parent_catenax_id              TYPE TEXT,
    ALTER COLUMN process_id                     TYPE TEXT,
    ALTER COLUMN child_catenax_id               TYPE TEXT,
    ALTER COLUMN lifecycle_context              TYPE TEXT,
    ALTER COLUMN assembled_on                   TYPE TEXT,
    ALTER COLUMN measurement_unit_lexical_value TYPE TEXT,
    ALTER COLUMN shell_id                       TYPE TEXT,
    ALTER COLUMN data_type_uri                  TYPE TEXT;

 ALTER TABLE failure_log
    ALTER COLUMN uuid       TYPE TEXT,
    ALTER COLUMN process_id TYPE TEXT,
    ALTER COLUMN log        TYPE TEXT;

 ALTER TABLE process_report
    ALTER COLUMN process_id TYPE TEXT,
    ALTER COLUMN csv_type   TYPE TEXT,
    ALTER COLUMN status     TYPE TEXT;