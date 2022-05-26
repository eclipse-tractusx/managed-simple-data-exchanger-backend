/*
 * Copyright 2022 CatenaX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


CREATE TABLE aspect
(
    uuid VARCHAR(50) PRIMARY KEY,
    process_id VARCHAR(50),
    part_instance_id VARCHAR(50),
    manufacturing_date VARCHAR(50),
    manufacturing_country VARCHAR(50),
    manufacturer_part_id VARCHAR(50),
    customer_part_id VARCHAR(50),
    classification VARCHAR(50),
    name_at_manufacturer VARCHAR(50),
    name_at_customer VARCHAR(50),
    optional_identifier_key VARCHAR(50),
    optional_identifier_value VARCHAR(50),
    shell_id VARCHAR(50)
);

CREATE TABLE aspect_relationship
(
    parent_catenax_id VARCHAR(50),
    process_id VARCHAR(50),
    child_catenax_id VARCHAR(50),
    lifecycle_context VARCHAR(50),
    assembled_on VARCHAR(50),
    quantity_number NUMERIC(10,5),
    measurement_unit_lexical_value VARCHAR(50),
    shell_id VARCHAR(50),
    data_type_uri VARCHAR(50),
    PRIMARY KEY(parent_catenax_id, child_catenax_id)
);

CREATE TABLE failure_log
(
    uuid VARCHAR(50) PRIMARY KEY,
    process_id VARCHAR(50),
    log VARCHAR(5000),
    date_time timestamp
);

CREATE TABLE process_report
(
    process_id VARCHAR(50) PRIMARY KEY,
    csv_type VARCHAR(50),
    number_of_items INTEGER,
    number_of_failed_items INTEGER,
    number_of_succeeded_items INTEGER,
    status VARCHAR(50),
    start_date timestamp,
    end_date timestamp
);
