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

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "aspect_relationship")
@Data
@IdClass(AspectRelationshipPrimaryKey.class)
public class AspectRelationshipEntity {

    @Id
    private String parentCatenaXId;
    @Id
    private String processId;
    private String childCatenaXId;
    private String lifecycleContext;
    private String assembledOn;
    private String quantityNumber;
    private String measurementUnitLexicalValue;
    private String dataTypeUri;
}

@Data
class AspectRelationshipPrimaryKey implements Serializable {

    private String parentCatenaXId;
    private String childCatenaXId;
}


