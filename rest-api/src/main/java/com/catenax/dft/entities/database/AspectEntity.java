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

package com.catenax.dft.entities.database;

import com.catenax.dft.enums.OptionalIdentifierKeyEnum;
import lombok.Data;


import javax.persistence.*;


@Table(name = "aspect")
@Entity
@Data
public class AspectEntity {

    @Id
    private String uuid;
    private String processId;
    private String partInstanceId;
    private String manufacturingDate;
    private String manufacturingCountry;
    private String manufacturerPartId;
    private String customerPartId;
    private String classification;
    private String nameAtManufacturer;
    private String nameAtCustomer;
    @Convert(attributeName = "prettyName")
    private OptionalIdentifierKeyEnum optionalIdentifierKey;
    private String optionalIdentifierValue;
}
