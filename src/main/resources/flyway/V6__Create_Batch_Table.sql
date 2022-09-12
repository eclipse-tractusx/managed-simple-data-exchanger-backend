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

CREATE TABLE batch
(
    uuid                      VARCHAR(50) PRIMARY KEY,
    process_id                VARCHAR(50),
    batch_id                  VARCHAR(50),
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