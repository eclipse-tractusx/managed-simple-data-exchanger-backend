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


CREATE TABLE aspect
(
    uuid                      VARCHAR(50) PRIMARY KEY,
    process_id                VARCHAR(50),
    part_instance_id          VARCHAR(50),
    manufacturing_date        VARCHAR(50),
    manufacturing_country     VARCHAR(50),
    manufacturer_part_id      VARCHAR(50),
    customer_part_id          VARCHAR(50),
    classification            VARCHAR(50),
    name_at_manufacturer      VARCHAR(50),
    name_at_customer          VARCHAR(50),
    optional_identifier_key   VARCHAR(50),
    optional_identifier_value VARCHAR(50),
    shell_id                  VARCHAR(50)
);

CREATE TABLE aspect_relationship
(
    parent_catenax_id              VARCHAR(50),
    process_id                     VARCHAR(50),
    child_catenax_id               VARCHAR(50),
    lifecycle_context              VARCHAR(50),
    assembled_on                   VARCHAR(50),
    quantity_number                NUMERIC(10, 5),
    measurement_unit_lexical_value VARCHAR(50),
    shell_id                       VARCHAR(50),
    data_type_uri                  VARCHAR(50),
    PRIMARY KEY (parent_catenax_id, child_catenax_id)
);

CREATE TABLE failure_log
(
    uuid       VARCHAR(50) PRIMARY KEY,
    process_id VARCHAR(50),
    log        VARCHAR(5000),
    date_time  timestamp
);

CREATE TABLE process_report
(
    process_id                VARCHAR(50) PRIMARY KEY,
    csv_type                  VARCHAR(50),
    number_of_items           INTEGER,
    number_of_failed_items    INTEGER,
    number_of_succeeded_items INTEGER,
    status                    VARCHAR(50),
    start_date                timestamp,
    end_date                  timestamp
);
