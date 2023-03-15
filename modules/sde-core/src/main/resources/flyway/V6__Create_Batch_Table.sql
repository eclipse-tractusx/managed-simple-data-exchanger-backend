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