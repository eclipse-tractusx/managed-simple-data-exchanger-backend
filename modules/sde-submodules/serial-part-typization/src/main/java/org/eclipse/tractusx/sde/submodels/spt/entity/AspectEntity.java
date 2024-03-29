/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022, 2024 T-Systems International GmbH
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.sde.submodels.spt.entity;

import org.eclipse.tractusx.sde.common.entities.CommonPropEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "aspect")
@Entity
@Data
public class AspectEntity extends CommonPropEntity {

    @Id
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "part_instance_id")
    private String partInstanceId;
    @Column(name = "manufacturing_date")
    private String manufacturingDate;
    @Column(name = "manufacturing_country")
    private String manufacturingCountry;
    @Column(name = "manufacturer_part_id")
    private String manufacturerPartId;
    @Column(name = "customer_part_id")
    private String customerPartId;
    @Column(name = "classification")
    private String classification;
    @Column(name = "name_at_manufacturer")
    private String nameAtManufacturer;
    @Column(name = "name_at_customer")
    private String nameAtCustomer;
    @Column(name = "optional_identifier_key")
    private String optionalIdentifierKey;
    @Column(name = "optional_identifier_value")
    private String optionalIdentifierValue;
}
