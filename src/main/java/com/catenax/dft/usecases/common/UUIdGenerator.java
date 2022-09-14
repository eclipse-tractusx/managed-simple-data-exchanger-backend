/********************************************************************************
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.catenax.dft.usecases.common;

import java.util.UUID;

public class UUIdGenerator {

    public static final String URN_UUID_PREFIX = "urn:uuid:";

    private UUIdGenerator() {
    }

    public static String getUrnUuid() {
        return getPrefixedUuid(URN_UUID_PREFIX);
    }

    private static String getPrefixedUuid(String prefix) {
        return String.format("%s%s", prefix, UUID.randomUUID());
    }

    public static String getUuid() {
        return getPrefixedUuid("");
    }
}
